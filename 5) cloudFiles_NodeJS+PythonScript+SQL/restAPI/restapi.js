const express = require('express')
const bodyParser = require('body-parser')
const { inspect } = require('util')
var multer = require('multer');
var fs = require("fs")
//predict images & audio save location
var uploadImg = multer({dest: './images/'});
var uploadAudio = multer({dest: './audios/'});
const shell = require('shelljs')
const app = express()
const {
    createPool
} = require('mysql2');
const { fstat } = require('fs');
const nodemailer = require('nodemailer');
var transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
      user: 'email@email.com',
      pass: 'password'
    }
  });

const port = 8081

//post request
app.use(bodyParser.urlencoded({ extended: true, limit: '50mb' }));
app.use(express.json({limit: '50mb'}));

//db profile
const pool = createPool({
    host: '127.0.0.1',
    user: 'user',
    password: 'sqlPassword',
    database: 'dogsocialmedia'
})

//exec command
function exec(cmd, infoArr, res) {
    shell.exec(cmd, (code, stdout, stderr) => {
	if (stderr) {
        res.send("[!] Error in execution")
	} else {
        checkSick(stdout,infoArr, res)
	}
    })
}

//check if the predicted dog is sick, sent email to notify dog's owner
function checkSick(predictrsl,infoArr, res){
    var friendname = infoArr[0]
    var imgDir = infoArr[1]
    var imgname = infoArr[2]
    var currentuser = infoArr[3]
    
    res.send(predictrsl)

    //insert prediction into db
    var sql = "INSERT INTO PredictionData (report_name, username, friend, predicted_label) VALUES (?, ?, ?, ?)"

    let ts = Date.now();
    let date_ob = new Date(ts);
    let date = date_ob.getDate();
    let month = date_ob.getMonth() + 1;
    let year = date_ob.getFullYear();

    var report_date = year + "-" + month + "-" + date
    var reportname = report_date + "_" + currentuser + "_" + friendname

    pool.query(sql, [reportname, currentuser, friendname, predictrsl], (err, result, field) => {
        if(err){
            return console.log("[!] ERROR in INSERT PredictionData: " + err.message)
        }
                   
    })

    // if dog is sick (get predict result from exec func)
    if(predictrsl.includes("sick")){
        var sql = "SELECT email FROM Profiles WHERE username=?"

        pool.query(sql, [friendname], (err, result, field) =>{
            if(err){
                return console.log("[!] ERROR in /predict: " + err.message)
            }
        
            if(result.length > 0){
                var friendemail = result[0]['email']
                
                //send email
                var mailOptions = {
                    from: 'email@email.com',
                    to: friendemail,
                    subject: '[*] Check Your Dog Right Now',
                    text: 'Your dog looks ill. Please take it to the vet ASAP!">',
                    attachments: [{
                        filename: 'dogImage.png',
                        path: imgDir + "/" + imgname,
                        cid: 'dogimage' 
                   }]
                };

                transporter.sendMail(mailOptions, function(error, info){
                    if (error) {
                      console.log(error);
                    } else {
                      console.log('Email sent: ' + info.response);
                    }
                  })
            }
        })
    }
}

//restAPI
app.get('/', function(req, res){
    res.send("Welcome to the Dog Social Media Rest API\n")

})

app.post('/signup', (req, res) =>{
    var username = req.body.username
    var password = req.body.password
    var email = req.body.email
    var ip = req.body.ip
    var port = req.body.port

    var sql = "INSERT INTO Profiles(username, passwd, email, ip, port) VALUES(?,?,?,?,?)"

    pool.query(sql, [username, password, email, ip, port], (err, result, field) => {
        if(err){
            res.send("[!] ERROR in /signup: " + err.message)
            return console.log("[!] ERROR in /signup: " + err.message)
        }

        res.send("Signup Successful!")
                   
    })
})

app.post('/login', (req, res) => {
    var username = req.body.username
    var passwd = req.body.password

    var sql = "SELECT * FROM Profiles WHERE username=? AND passwd=?"

    pool.query(sql, [username, passwd], (err, result, field) => {
        if(err){
            res.send("[!] ERROR in /login: " + err.message)
            return console.log("[!] ERROR in /login: " + err.message)
        }

        if(result.length == 1){
            res.send("Valid Credential")
        }else{
            res.send("Invalid Credential")
        }
        
    })
})

app.post('/getProfileDet', (req, res) => {
    var username = req.body.username

    var sql = "SELECT username, email, ip, port FROM Profiles WHERE username=?"

    pool.query(sql, [username], (err, result, field) =>{
        if(err){
            res.send("[!] ERROR in /getProfileDet: " + err.message)
            return console.log("[!] ERROR in /getProfileDet: " + err.message)
        }

        if(result.length > 0){
            res.send(result[0])
        }else{
            res.send("Username not found!")
        }
    })
})

app.post('/changePw', (req, res) =>{
    var username = req.body.username
    var oldpw = req.body.oldpw
    var newpw = req.body.newpw

    var check_sql = "SELECT * FROM Profiles WHERE username=? AND passwd=?"
    var changePW_sql = "UPDATE Profiles SET passwd=? WHERE username=?"

    pool.query(check_sql, [username, oldpw], (err, result, field) =>{
        if(err){
            res.send("[!] ERROR in /changePw: " + err.message)
            return console.log("[!] ERROR in /changePw: " + err.message)
        }
        
        //valid old credential
        if(result.length >0){
            pool.query(changePW_sql, [newpw, username], (err, result, field) =>{
                if(err){
                    res.send("[!] ERROR in /changePw: " + err.message)
                    return console.log("[!] ERROR in /changePw: " + err.message)
                }

                res.send("Password Changed!")
            })
        }else{ //invalid old credential 
            res.send("[!] Old Credential Not Match!")
        }
    })
})

app.post('/updateConnVal', (req, res) =>{
    var username = req.body.username
    var ip = req.body.ip
    var port = req.body.port
    
    var sql = "UPDATE Profiles SET ip=?, port=? WHERE username=?"

    pool.query(sql, [ip, port, username], (err, result, field) => {
        if(err){
            res.send("[!] ERROR in /updateConnVal: " + err.message)
            return console.log("[!] ERROR in /updateConnVal: " + err.message)
        }

        res.send("Update Connection Value Successsful!")
    })
})

app.get('/searchFriends', (req, res) => {
    var username = req.query.username

    var sql = "SELECT username FROM Profiles WHERE username like ?"

    pool.query(sql, ['%' + username + '%'], (err, result, field) => {
        if(err){
            res.send("[!] ERROR in /searchFriends: " + err.message)
            return console.log("[!] ERROR in /searchFriends: " + err.message)
        }

        if(result.length > 0){
            var jsonObject = {}
            jsonObject['query'] = result
            res.send(jsonObject)
        }else{
            res.send("[*] No user found")
        }
    })
})

app.post('/addFriend', (req, res) => {
    var username = req.body.username
    var addUser = req.body.addUser
    
    var checkAdded = "SELECT * FROM Friends WHERE username=? AND friend_username = ?"
    var sql = "INSERT INTO Friends(username, friend_username) VALUES (?, ?)"

    //check friend added to prevent duplication
    pool.query(checkAdded, [username, addUser], (err, result, field) => {
        if(err){
            res.send("[!] ERROR in /addFriend: " + err.message)
            return console.log("[!] ERROR in /addFriend: " + err.message)
        }
        
        //no friend duplication, proceed to adding
        if(result.length == 0){
            pool.query(sql, [username, addUser], (err, result, field) => {
                if(err){
                    res.send("[!] ERROR in /addFriend: " + err.message)
                    return console.log("[!] ERROR in /addFriend: " + err.message)
                }
                
                //another way round (both side have connection)
                pool.query(sql, [addUser, username], (err, result, field) => {
                    if(err){
                        res.send("[!] ERROR in /addFriend: " + err.message)
                        return console.log("[!] ERROR in /addFriend: " + err.message)
                    }
            
                    res.send("[*] Friend Successfully Added.")
                })
            })
        }else{ //prompt error, if added before
            res.send("[!] Friend Added Before!")
        }
    })
    
})

app.get('/friendList', (req, res) => {
    var username = req.query.username

    var sql = "SELECT friend_username FROM Friends WHERE username=? "

    pool.query(sql, [username], (err, result, field) => {
        if(err){
            res.send("[!] ERROR in /friendList: " + err.message)
            return console.log("[!] ERROR in /friendList: " + err.message)
        }

        if(result.length > 0){
            var jsonObject = {}
            jsonObject['friends'] = result
            res.send(jsonObject)
        }else{
            res.send("[*] You've No Friends. Boo....")
        }
    })
})

//capture audio filename var
var prevUploadAudioFN = ""

app.post('/predict', uploadImg.single('file'), function(req, res){
    if(req.file){
        var imgname = req.file.filename
        
        classifierScript = "/home/cloudsystester/cloudFiles/dogEmotionClassifier.py"
        imgDir = "/home/cloudsystester/cloudFiles/restAPI/images"
        audioDir = "/home/cloudsystester/cloudFiles/restAPI/audios"
        
        const cmd = `python3 ${classifierScript} "${imgDir}/${imgname}" "${audioDir}/${prevUploadAudioFN}"`
        
        // extracting username, friend's name & email
        var oriImgName = req.file.originalname
        var tmp = oriImgName.split('_')
        var loginuser = tmp[1]
        var friendname = tmp[2]
        
        var infoArr = [friendname, imgDir, imgname, loginuser]

        //console.log(infoArr)
        exec(cmd,infoArr, res)

    }else{
        res.send("image upload unsuccessful\n")
    }        
})

app.post('/uploadAudio', uploadAudio.single('file'), function(req, res){
    if(req.file){
        var audioname = req.file.filename
	    //console.log(audioname)
        prevUploadAudioFN = audioname

        res.send("audio upload sucessful\n")
    }else{
        res.send("audio upload unsuccessful\n")
    }        
})

//data visualization
app.get('/getReportLists', (req, res) => {
    var username = req.query.username

    var sql = 'select distinct report_name from PredictionData where username=?'

    pool.query(sql, [username], (err, result, field) => {
        if(err){
            res.send("[!] ERROR in /getReportLists: " + err.message)
            return console.log("[!] ERROR in /getReportLists: " + err.message)
        }

        if(result.length > 0){
            var jsonObject = {}
            jsonObject['reports'] = result
            res.send(jsonObject)
        }else{
            res.send("[*] You've no report.")
        }
    })
})

app.get('/getReport', (req, res) => {
    var reportname = req.query.reportname

    var sql = 'SELECT predict_date, predicted_label FROM PredictionData WHERE report_name=?'

    pool.query(sql, [reportname], (err, result, field) => {
        if(err){
            res.send("[!] ERROR in /getReport: " + err.message)
            return console.log("[!] ERROR in /getReport: " + err.message)
        }

        if(result.length > 0){
            var jsonObject = {}
            jsonObject['data'] = result
            res.send(jsonObject)
        }
    })
})

app.listen(port)
console.log('listening on port 8081')
