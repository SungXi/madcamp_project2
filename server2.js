var mongodb = require('mongodb');
var fs = require('fs');
var ObjectID = mongodb.ObjectID;
var crypto = require('crypto');
var express = require('express');
var multer = require('multer');
var bodyParser = require('body-parser');
var path = require('path');

function KL_divergence(OrgDist, DstDist) {
    var result = 0.0;
    var log = 0.0;

    for (var j = 0; j < 360; j++) {
        if (DstDist[j] == 0) {
            continue;
        }
        log = Math.log(OrgDist[j] / DstDist[j] + 0.01);
        result = result + OrgDist[j] * log;
    }
    return result;
}

function KL_divergence2(OrgDist, DstDist) {
    var result = 0.0;
    var log = 0.0;

    for (var j = 0; j < 256; j++) {
        if (DstDist[j] == 0) {
            continue;
        }
        log = Math.log(OrgDist[j] / DstDist[j] + 0.01);
        result = result + OrgDist[j] * log;
    }
    return result;
}

function correlation(OrgDist, DstDist, step) {
    var result = 0.0;
    var up = 0.0;
    var down1 = 0.0;
    var down2 = 0.0;

    // Numerator calculation
    for (var j = 0; j < step; j ++) {
        up = up + ((OrgDist[j] - (1 / step)) * (DstDist[j] - (1 / step)))
    }

    // Denominator calculation
    for (var j = 0; j < step; j ++) {
        down1 = down1 + Math.pow((OrgDist[j] - (1 / step)), 2);
    }

    for (var j = 0; j < step; j ++) {
        down2 = down2 + Math.pow((DstDist[j] - (1 / step)), 2);
    }

    result = up / Math.sqrt(down1 * down2);

    return result;
}

var resultList = [
    { index: 0, cost: 0 },
    { index: 0, cost: 0 },
    { index: 0, cost: 0 },
    { index: 0, cost: 0 },
    { index: 0, cost: 0 }
];

var generateRandomString = function (length) {
    return crypto.randomBytes(Math.ceil(length / 2))
        .toString('hex')
        .slice(0, length);
};

var sha512 = function (password, salt) {
    var hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    var value = hash.digest('hex');
    return {
        salt: salt,
        passwordHash: value
    };
};

function saltHashPassword(userPassword) {
    var salt = generateRandomString(16);
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function checkHashPassword(userPassword, salt) {
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

var app = express();
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ limit: '50mb', extended: true }));

storage = multer.diskStorage({
    destination: './uploads/',
    filename: function(req, file, cb) {
        return crypto.pseudoRandomBytes(16, function(err, raw) {
            if (err) {
                return cb(err);
            }
            return cb(null, "" + (raw.toString('hex')) + (path.extname(file.originalname)));
        });
    }
});
var upload = multer({ storage: storage });

var MongoClient = mongodb.MongoClient;
var url = 'mongodb://localhost:27017/';
MongoClient.connect(url, { useNewUrlParser: true }, function (error, client) {
    if (error) {
        console.log('Unable to connect to the mongoDB server.Error', error);
    } else {
        //Routes
        app.get('/', function (request, response) {
            response.json({ message: 'Welcome!' });
            response.end();
        });
        //Register
        app.post('/register', (request, response, next) => {
            var post_data = request.body;
            var plain_password = post_data.password;
            var hash_data = saltHashPassword(plain_password);
            var password = hash_data.passwordHash;
            var salt = hash_data.salt;
            var name = post_data.name;
            var email = post_data.email;
            var insertJson = {
                'email': email,
                'password': password,
                'salt': salt,
                'name': name
            };
            var db = client.db('AppDB');
            db.collection('user')
                .find({ 'email': email }).count(function (error, number) {
                    if (number != 0) {
                        response.json('중복된 이메일입니다. 다른 이메일을 사용해주세요.');
                        response.end();
                        console.log('Given email already exists.');
                    } else {
                        db.collection('user')
                            .insertOne(insertJson, function (error, res) {
                                response.json('등록 완료! 새로 로그인해주세요.');
                                response.end();
                                console.log('Registration success.');
                            })
                    }
            });
        });
        //Login
        app.post('/login', (request, response, next) => {
            var post_data = request.body;
            var email = post_data.email;
            var userPassword = post_data.password;
            var db = client.db('AppDB');
            db.collection('user')
                .find({ 'email': email }).count(function (error, number) {
                if (number == 0) {
                    response.json('아이디/이메일을 찾을 수 없습니다.');
                    response.end();
                    console.log('Given email not exists.');
                } else {
                    db.collection('user')
                        .findOne({ 'email': email }, function (error, user) {
                            var salt = user.salt;
                            var hashed_password = checkHashPassword(userPassword, salt).passwordHash;
                            var encrypted_password = user.password;
                            if (hashed_password == encrypted_password) {
                                response.json('로그인 완료!');
                                response.end();
                                console.log('Login success.');
                            } else {
                                response.json('비밀번호가 다릅니다.');
                                response.end();
                                console.log('Wrong password.');
                            }
                        })
                }
            });
        });
        // [TAB 1] Refresh request
        app.post('/getPeople', (req, res, next) => {
            console.log('---------- FETCH ----------');
            var post_data = req.body;
            var owner_email = post_data.owner_email;
            var db = client.db('AppDB');
            db.collection('contact').findOne({ 'owner_email': owner_email }, function (error, user) {
                if (user == null) {
                    res.end();
                    console.log('No contacts in DB.');
                } else {
                    var dataJson = user.data;
                    res.json(dataJson);
                    res.end();
                    console.log('Contact fetched');
                    console.log(dataJson);
                }
                console.log('---------- FETCH Done ----------');
            });
        });
        // [TAB 1] Add Request
        app.post('/addPerson', (req, res, next) => {
            console.log('---------- ADD ----------');
            var post_data = req.body;
            var name = post_data.name;
            var phone_number = post_data.number;
            var email = post_data.email;
            var owner_email = post_data.owner_email;
            var db = client.db('AppDB');
            db.collection('contact').find({ 'owner_email': owner_email }).count(function (error, number) {
                if (number == 0) {
                    var insertJson = {
                        'owner_email': owner_email,
                        'data': [
                            { 'name': name,'number': phone_number, 'email': email }
                        ]
                    };
                    db.collection('contact')
                        .insertOne(insertJson, function (error, result) {
                            console.log('Contact inserted, new owner');
                            res.json(name + phone_number + email);
                            res.end();
                        });
                } else {
                    var insertJson = {
                        'data': {
                            $each: [ { 'name': name,'number': phone_number, 'email': email } ],
                            $sort: { 'name': -1 }
                        }
                    };
                    db.collection('contact')
                        .updateOne({ 'owner_email': owner_email },
                            { $push: insertJson }, function (error, result) {
                                console.log('Contact inserted, existing owner');
                                res.json(name + phone_number + email);
                                res.end();
                            });
                }
                console.log('---------- ADD Done ----------');
            });
        });
        // [TAB 1] Delete Request
        app.post('/deletePerson', (req, res, next) => {
            console.log('---------- DELETE ----------');
            var post_data = req.body;
            var name = post_data.name;
            var phone_number = post_data.number;
            var email = post_data.email;
            var owner_email = post_data.owner_email;
            var db = client.db('AppDB');
            db.collection('contact').find( { 'owner_email': owner_email,
                'data.name': name, 'data.number': phone_number, 'data.email': email }).count(function (error, number) {
                if (number == 0) {
                    res.end();
                    console.log(error);
                } else {
                    db.collection('contact').updateOne({ 'owner_email': owner_email },
                        { $pull: { data: { 'name': name, 'number': phone_number, 'email': email } } });
                    console.log('Contact deleted');
                    res.json(name + phone_number + email);
                    res.end();
                }
                console.log('---------- DELETE Done ----------');
            })
        });
        // [TAB 1] Edit Request
        app.post('/editPerson', (req, res, next) => {
            console.log('---------- EDIT ----------');
            var post_data = req.body;
            var originName = post_data.origin_name;
            var originPhone_number = post_data.origin_number;
            var originEmail = post_data.origin_email;
            var owner_email = post_data.owner_email;
            var db = client.db('AppDB');
            db.collection('contact').find({ 'owner_email': owner_email,
                'data.name': originName, 'data.number': originPhone_number, 'data.email': originEmail }).count(function (error, number) {
                if (number == 0) {
                    res.end();
                    console.log(error);
                } else {
                    var newName = post_data.new_name;
                    var newPhone_number = post_data.new_number;
                    var newEmail = post_data.new_email;
                    db.collection('contact').updateOne({ 'owner_email': owner_email },
                        { $pull: { data: { 'name': originName, 'number': originPhone_number, 'email': originEmail } } });
                    var insertJson = {
                        'data': {
                            $each: [ { 'name': newName,'number': newPhone_number, 'email': newEmail } ],
                            $sort: { 'name': -1 }
                        }
                    };
                    db.collection('contact')
                        .updateOne({ 'owner_email': owner_email },
                            { $push: insertJson }, function (error, result) {
                                console.log('Contact edited');
                                res.json(newName + newPhone_number + newEmail);
                                res.end();
                            });
                }
                console.log('---------- EDIT Done ----------');
            })
        });
        // [TAB 1] Remove All
        app.post('/removePeople', (req, res, next) => {
            console.log('---------- REMOVE ----------');
            var post_data = req.body;
            var owner_email = post_data.owner_email;
            var db = client.db('AppDB');
            var result = db.collection('contact').removeOne( { 'owner_email': owner_email });
            res.json(result);
            res.end();
            console.log('---------- REMOVE Done ----------');
        });

        /* Using Multer */
        // Upload image
        app.post(
            '/upload_image_request',
            multer({
                storage: storage
            }).single('upload'), function(req, res) {
                var post_data = req.body;
                var owner_email = post_data.owner_email;
                var path = "/uploads/" + req.file.filename;
                res.redirect(path);
                console.log(req.file.filename + " : " + owner_email);
                var db = client.db('AppDB');
                try {
                    db.collection('images').find({ 'owner_email': owner_email }).count(function (error, number) {
                        if (number == 0) {
                            var insertJson = {
                                'owner_email': owner_email,
                                'data': [
                                    {'path': path }
                                ]
                            };
                            db.collection('images')
                                .insertOne(insertJson, function (error, result) {
                                    console.log('Image inserted, new owner');
                                });
                        } else {
                            var insertJson = {
                                'data': {
                                    $each: [{'path': path}]
                                }
                            };
                            db.collection('images')
                                .updateOne({'owner_email': owner_email},
                                    {$push: insertJson}, function (error, result) {
                                        console.log('Image inserted, existing owner');
                                    });
                        }
                    });
                } catch (e) {
                    console.log(e);
                    return res.status(500).end();
                }
                return res.status(200).end();
            });
        app.get('/uploads/:upload', function (req, res) {
            var file = req.params.upload;
            console.log(req.params.upload);
            var img = fs.readFileSync(__dirname + "/uploads/" + file);
            res.writeHead(200, {'Content-Type': 'image/png' });
            res.end(img, 'binary');
        });
        // Download image
        app.post('/fetch_images_request', (req, res, next) => {
            var post_data = req.body;
            var owner_email = post_data.owner_email;
            var db = client.db('AppDB');
            db.collection('images').findOne({ 'owner_email': owner_email }, function (error, user) {
                if (user == null) {
                    console.log('No images in DB.');
                    res.end();
                } else {
                    var dataJson = user.data;
                    res.json(dataJson);
                    console.log('Image fetched');
                }
            });
        });
        // Delete image
        app.post('/delete_image_request', (req, res, next) => {
            var post_data = req.body;
            var owner_email = post_data.owner_email;
            var path = post_data.path;
            var db = client.db('AppDB');
            fs.unlink("." + path, (error) => {
                console.log('Image deleted');
                console.log(path);
            });
            db.collection('images').find({ 'owner_email': owner_email, 'data.path': path }).count(function (error, number) {
                if (number == 0) {
                    res.json("이미지가 존재하지 않습니다.");
                    res.end();
                    console.log(error);
                } else {
                    db.collection('images').updateOne({ 'owner_email': owner_email },
                        { $pull: { data: { 'path': path } } });
                    res.json("삭제 완료!");
                    res.end();
                    console.log('Path deleted');
                }
            })
        });
        // [TAB 2] Fetch images
        app.post('/fetch_images', (request, response, next) => {
           var post_data = request.body;
           var email = post_data.email;
           var db = client.db('AppDB');
           db.collection('image').findOne({ 'owner_email': email }, function (error, user) {
               if (user == null) {
                   console.log('No images in DB.');
               } else {
                   var dataJson = user.images;
                   response.json(dataJson);
                   console.log('Fetching success.');
               }
           });
        });
        // [TAB 2] Upload image
        app.post('/upload_image', (request, response, next) => {
            var post_data = request.body;
            var data = post_data.image;
            var path = post_data.path;
            var email = post_data.email;
            var db = client.db('AppDB');
            db.collection('image')
                .find({ 'owner_email': email }).count(function (error, number) {
                if (number == 0) {
                    var insertJson = {
                        'owner_email': email,
                        'images': [
                            { 'path': path,'image': data }
                        ]
                    };
                    db.collection('image')
                        .insertOne(insertJson, function (error, result) {
                            response.json('사진 저장 완료!');
                            console.log('Insertion success.');
                        });
                } else {
                    db.collection('image')
                        .find({ 'owner_email': email, 'images.path': path }).count(function (error, number) {
                            if (number != 0) {
                                response.json('중복된 사진이 존재합니다.');
                                console.log('Duplicate exists.');
                            } else {
                                var insertJson = {
                                    'images': {
                                        $each: [ {'path': path, 'image': data} ],
                                        $sort: { 'path': -1 }
                                    }
                                };
                                db.collection('image')
                                    .updateOne({ 'owner_email': email },
                                        { $push: insertJson }, function (error, result) {
                                        response.json('사진 저장 완료!');
                                        console.log('Insertion success.');
                                    });
                            }
                    });
                }
            });
        });

        // [TAB 3] Find proper images matching with original image
        app.post('/findImage', async(request, response, next) => {
            var post_data = request.body;
            var feature1 = post_data.feature1;
            var feature2 = post_data.feature2;
            var data1 = JSON.parse(feature1);
            var data2 = JSON.parse(feature2);
            var db = client.db('AppDB');

            resultList = [
                { image_url: 0, cost: -999 },
                { image_url: 0, cost: -999 },
                { image_url: 0, cost: -999 },
                { image_url: 0, cost: -999 },
                { image_url: 0, cost: -999 }
            ];

            await db.collection('imageDB').find().forEach(function(doc) {
                if (doc != null) {
                    // Calculate KL-Divergence
                    var correlation1 = correlation(data1, doc.feature1, 360);
                    var correlation2 = correlation(data2, doc.feature2, 256);
                    if (correlation1 + correlation2 > resultList[0].cost) {
                        var sortingField = "cost";
                        resultList.splice(0, 1);
                        resultList.push({image_url: doc.image_url, cost: correlation1 + correlation2});
                        //console.log(resultList);
                        resultList.sort(function (a, b) {
                            return a[sortingField] - b[sortingField];
                        });
                    }
                }
            });

            // Should URLs back to the client
            var returnJsonArray = [resultList[4].image_url, resultList[3].image_url, resultList[2].image_url, resultList[1].image_url, resultList[0].image_url];
            console.log(returnJsonArray);
            response.json(returnJsonArray);
            response.end();
        });
        // [TAB 3] Add image to DB
        app.post(
            '/addDB',
            multer({
                storage: storage
            }).single('upload'), function(req, res) {
                var post_data = req.body;
                var path = "/uploads/" + req.file.filename;
                var feature1 = post_data.feature1;
                var feature2 = post_data.feature2;
                var data1 = JSON.parse(feature1);
                var data2 = JSON.parse(feature2);
                res.redirect(path);
                console.log(req.file.filename);
                var db = client.db('AppDB');
                try {
                    var insertJson = {
                        'image_url': path,
                        'feature1': data1,
                        'feature2': data2
                    };
                    db.collection('imageDB')
                        .insertOne(insertJson, function (error, result) {
                            console.log('DB inserted');
                        });
                } catch (e) {
                    console.log(e);
                    return res.status(500).end();
                }
                return res.status(200).end();
            });

        //Web Server
        app.listen(3000, () => {
            console.log('Connected to MongoDB Server, WebService running on port 3000');
        });
    }
});
