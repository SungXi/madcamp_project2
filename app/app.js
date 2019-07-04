var mongodb = require('mongodb');
var ObjectID = mongodb.ObjectID;
var crypto = require('crypto');
var express = require('express');
var bodyParser = require('body-parser');

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
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

var MongoClient = mongodb.MongoClient;
var url = 'mongodb://localhost:27017';
MongoClient.connect(url, { useNewUrlParser: true }, function (error, client) {
    if (error) {
        console.log('Unable to connect to the mongoDB server.Error', error);
    } else {
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
                        console.log('Given email already exists.');
                    } else {
                        db.collection('user')
                            .insertOne(insertJson, function (error, res) {
                                response.json('등록 완료! 새로 로그인해주세요.');
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
                    console.log('Given email not exists.');
                } else {
                    db.collection('user')
                        .findOne({ 'email': email }, function (error, user) {
                            var salt = user.salt;
                            var hashed_password = checkHashPassword(userPassword, salt).passwordHash;
                            var encrypted_password = user.password;
                            if (hashed_password == encrypted_password) {
                                response.json('로그인 완료!');
                                console.log('Login success.');
                                console.log(request.body);
                            } else {
                                response.json('비밀번호가 다릅니다.');
                                console.log('Wrong password.');
                            }
                        })
                }
            });
        });
        //Web Server
        app.listen(3000, () => {
            console.log('Connected to MongoDB Server, WebService running on port 3000');
        });
    }
});

