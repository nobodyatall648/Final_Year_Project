-- local accessing 
--- CREATE USER 'user'@'localhost' IDENTIFIED BY 'mySecretPassword';
--- GRANT ALL PRIVILEGES ON *.* TO 'user'@'localhost';
--- FLUSH PRIVILEGES;

-- remote accessing
-- CREATE USER 'user'@'%' IDENTIFIED BY 'mySecretPassword';
-- GRANT ALL ON *.* TO 'user'@'%' IDENTIFIED BY 'mySecretPassword' WITH GRANT OPTION;
-- FLUSH PRIVILEGES;

CREATE DATABASE dogsocialmedia;

CREATE TABLE Profiles(
    username VARCHAR(30),
    passwd VARCHAR(30),
    email VARCHAR(50),
    ip VARCHAR(20),
    port VARCHAR(10),
    CONSTRAINT username_pk PRIMARY KEY(username)
);

CREATE TABLE Friends(
    id INTEGER AUTO_INCREMENT,
    username VARCHAR(30),
    friend_username VARCHAR(30),
    CONSTRAINT id_pk PRIMARY KEY(id),
    CONSTRAINT username_fk FOREIGN KEY (username) REFERENCES Profiles(username)
);

CREATE TABLE PredictionData(
    id INTEGER AUTO_INCREMENT,
    report_name VARCHAR(50),
    predict_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    username VARCHAR(30),
    friend VARCHAR(30),
    predicted_label VARCHAR(15),
    CONSTRAINT id_prediction_data_pk PRIMARY KEY(id),
    CONSTRAINT calleruser_fk FOREIGN KEY (username) REFERENCES Profiles(username)
);

--- insert some sample data
INSERT INTO Profiles VALUES ('micky', 'password', 'mickymail@gmail.com', '192.168.0.159', 1935);
INSERT INTO Profiles VALUES ('snowdog', 'snowden', 'dummysystemmail@gmail.com', '192.168.0.159', 1935);
INSERT INTO Profiles VALUES ('john', 'password', 'johnn@gmail.com', '192.168.0.189', 1935);
