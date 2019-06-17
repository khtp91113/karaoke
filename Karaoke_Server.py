from flask import Flask
from flask import request
import json
import MySQLdb
import datetime
app = Flask(__name__)


@app.route("/Login",methods={'GET','POST'})
def Login():
    db = connect_DB()
    cur = db.cursor()
    username = request.values["username"]
    passwd = request.values["passwd"]
    try:
        sql = "SELECT UID FROM KaraokeUsers WHERE Username=\""+username+"\" AND Passwd=\""+passwd+"\";"
        cur.execute(sql)
        UID = cur.fetchone()
        cur.close()
        db.close()
        if UID != None:
            return  "UID="+str(UID[0])
        else:
            return "UID=-1"

    except MySQLdb.Error, e:
        return "MySQL Error: %s "% str(e)


@app.route("/CreateAccount",methods={'GET','POST'})
def CreateAccount():
    db = connect_DB()
    cur = db.cursor()
    username = request.values["username"]
    passwd = request.values["passwd"]
    
    try:
        sql = "INSERT INTO KaraokeUsers(Username,Passwd) VALUE(\""+username+"\",\""+passwd+"\");"
        cur.execute(sql)
        db.commit()
        sql = "SELECT UID FROM KaraokeUsers WHERE Username=\""+username+"\" AND Passwd=\""+passwd+"\";"
        cur.execute(sql)
        UID = cur.fetchone()
        cur.close()
        db.close()
        return "UID="+str(UID[0])
    except MySQLdb.Error, e:
        return "MySQL Error: %s" % str(e)
    
@app.route("/ChangePasswd",methods={'GET','POST'})
def ChangePasswd():
    db = connect_DB()
    cur = db.cursor()
    UID = request.values["UID"]
    oldpw = request.values["oldpw"]
    newpw = request.values["newpw"]
    
    try:
        sql = "SELECT Passwd FROM KaraokeUsers WHERE UID=\""+UID+"\";"
        cur.execute(sql)
        passwd = cur.fetchone()
        if passwd[0]==oldpw:
            sql = "UPDATE KaraokeUsers SET Passwd='"+newpw+"' WHERE UID=\""+UID+"\";"
            cur.execute(sql)
            db.commit()
            cur.close()
            db.close()
            return "1,Change password sucessfully!"
        else:
            return "0,Passwd Error: Wrong password!"
    except MySQLdb.Error, e:
        return "0,MySQL Error: %s" % str(e)
    
@app.route("/QuerySongListByArtist",methods={'GET','POST'})
def QuerySongListByArtist():
    db = connect_DB()
    cur = db.cursor()
    Artist = request.values["Artist"]
    
    try:
        sql = "SELECT * FROM PublicMusic WHERE Artist=\""+Artist+"\";"
        cur.execute(sql)
        songlist = cur.fetchall()
        songlistjson = json.dumps(songlist);
        cur.close()
        db.close()
        return songlistjson
    except MySQLdb.Error, e:
        return "0,MySQL Error: %s" % str(e)
    
@app.route("/QuerySongListByGender",methods={'GET','POST'})
def QuerySongListByGender():
    db = connect_DB()
    cur = db.cursor()
    Gender = request.values["Gender"]
    
    try:
        sql = "SELECT * FROM PublicMusic WHERE Gender=\""+Gender+"\";"
        cur.execute(sql)
        songlist = cur.fetchall()
        songlistjson = json.dumps(songlist);
        cur.close()
        db.close()
        return songlistjson
    except MySQLdb.Error, e:
        return "0,MySQL Error: %s" % str(e)
    
@app.route("/QuerySongListByLanguage",methods={'GET','POST'})
def QuerySongListByLanguage():
    db = connect_DB()
    cur = db.cursor()
    Language = request.values["Language"]
    
    try:
        sql = "SELECT * FROM PublicMusic WHERE Language=\""+Language+"\";"
        cur.execute(sql)
        songlist = cur.fetchall()
        songlistjson = json.dumps(songlist);
        cur.close()
        db.close()
        return songlistjson
    except MySQLdb.Error, e:
        return "0,MySQL Error: %s" % str(e)

@app.route("/QuerySongListByCount",methods={'GET','POST'})
def QuerySongListByCount():
    db = connect_DB()
    cur = db.cursor()
    Language = request.values["Language"]
    
    try:
        sql = "SELECT * FROM PublicMusic WHERE Language=\""+Language+"\" ORDER BY COUNT DESC;"
        cur.execute(sql)
        songlist = cur.fetchall()
        songlistjson = json.dumps(songlist);
        cur.close()
        db.close()
        return songlistjson
    except MySQLdb.Error, e:
        return "0,MySQL Error: %s" % str(e)

@app.route("/check",methods={'GET'})
def check():
    db = connect_DB()
    cur = db.cursor()
    try:
        sql = "SELECT * FROM PublicMusic;"
        cur.execute(sql)
        songlist=cur.fetchall()
        songlistjson = json.dumps(songlist);
        cur.close()
        db.close() 
	return songlistjson
    except MySQLdb.Error, e:
        return str(e) 
# modified -- chun chi
@app.route("/QuerySong",methods={'GET','POST'})
def QuerySong():
    db = connect_DB()
    cur = db.cursor()
    MusicName = request.values["MusicName"]
    
    try:
        sql = "SELECT * FROM PublicMusic WHERE MusicName=\""+MusicName+"\";"
        cur.execute(sql)
        row_headers = [x[0] for x in cur.description]
        song = cur.fetchone()
        json_data = [dict(zip(row_headers, song))]
        songjson = json.dumps(json_data);
        MID = str(song[0])
        Count = str(song[8]+1)
        sql = "UPDATE PublicMusic SET Count=" + Count + " WHERE MID=" + MID + ";"
        cur.execute(sql)
        db.commit()
        cur.close()
        db.close()
        return songjson
    except MySQLdb.Error, e:
        return "0,MySQL Error: %s" % str(e)
 
# modified -- chun chi
@app.route("/UploadPersonalSong",methods={'GET','POST'})
def UploadPersonalSong():
    db = connect_DB()
    cur = db.cursor()
    PersonalMusicName = request.values["PersonalMusicName"] 
    UID = request.values["UID"]
    Path = '/home/ftpuser/Record/' + UID
    MusicName = PersonalMusicName.split("-")[1]
    
    try:
        sql = "SELECT * FROM PublicMusic WHERE MusicName=\""+MusicName+"\";"
        print sql
        cur.execute(sql)
        song = cur.fetchone()
        print song
        MID = str(song[0])
        
        sql = "REPLACE INTO PersonalMusic(PersonalMusicName, Path, UID, MID) VALUES('"+PersonalMusicName+"','"+Path+"',"+UID+", "+ MID +");"
        print sql
        cur.execute(sql)
        db.commit()
        cur.close()
        db.close()
        return Path
    except MySQLdb.Error, e:
        return "MySQL Error: %s" % str(e)
    
@app.route("/QueryPersonalSong",methods={'GET','POST'})
def QueryPersonalSong():
    db = connect_DB()
    cur = db.cursor()
    UID = request.values["UID"]
    PersonalMusicName = request.values["PersonalMusicName"] 
    
    try:
        sql = "SELECT * FROM PersonalMusic WHERE UID="+UID+" AND PersonalMusicName=\""+PersonalMusicName+"\";"
        cur.execute(sql)
        song = cur.fetchone()
        RecordPath = song[2]

        tmp = PersonalMusicName.split("-")

        sql = "SELECT * FROM PublicMusic WHERE MusicName=\""+tmp[1]+"\";"
        cur.execute(sql)
        song = cur.fetchone()
        LyricPath = song[4]

        result = [{"RecordPath": RecordPath, "LyricPath": LyricPath}]
        cur.close()
        db.close()
        return json.dumps(result)
    except MySQLdb.Error, e:
        return "MySQL Error: %s" % str(e)

@app.route("/QueryAllRecords",methods={'GET','POST'})
def QueryAllRecords():
    db = connect_DB()
    cur = db.cursor()
    
    try:
        sql = "SELECT * FROM KaraokeUsers;"
        cur.execute(sql)
        users = cur.fetchall()
        table = {}
        for user in users:
            UID = user[0]
            userName = user[1]
            table[UID] = userName
        sql = "SELECT * FROM PersonalMusic;"
        cur.execute(sql)
        song = cur.fetchall()
        row_headers = [x[0] for x in cur.description]
        json_data = []
        for data in song:
            d = dict(zip(row_headers, data))
            time_str = d['Timestamp'].strftime('%m/%d %H:%M:%S')
            d['Timestamp'] = time_str
            UID = data[4]
            userName = table[UID]
            d["userName"] = userName
            json_data.append(d)
        songjson = json.dumps(json_data);
        cur.close()
        db.close()
        return songjson
    except MySQLdb.Error, e:
        return "MySQL Error: %s" % str(e)
        
@app.route("/RemovePersonalSong",methods={'GET','POST'})
def RemovePersonalSong():
    db = connect_DB()
    cur = db.cursor()
    UID = request.values["UID"]
    PersonalMusicName = request.values["PersonalMusicName"] 
    
    try:
        sql = "SELECT Path FROM PersonalMusic WHERE UID="+UID+" AND PersonalMusicName=\""+PersonalMusicName+"\";"
        cur.execute(sql)
        tmp = cur.fetchone()
        sql = "DELETE FROM PersonalMusic WHERE UID="+UID+" AND PersonalMusicName=\""+PersonalMusicName+"\";"
        cur.execute(sql)
        db.commit()
        cur.close()
        db.close()
        return tmp[0]
    except MySQLdb.Error, e:
        return "MySQL Error: %s" % str(e)
        
@app.route("/QueryPersonalSongList",methods={'GET','POST'})
def QueryPersonalSongList():
    db = connect_DB()
    cur = db.cursor()
    UID = request.values["UID"]
    
    try:
        sql = "select PersonalMusic.UID,PublicMusic.MusicName,PublicMusic.Artist from PersonalMusic, PublicMusic where PersonalMusic.MID = PublicMusic.MID AND PersonalMusic.UID = "+UID+";"
        cur.execute(sql)
        songlist = cur.fetchall()
        songlistjson = json.dumps(songlist);
        cur.close()
        db.close()
        return songlistjson
    except MySQLdb.Error, e:
        return "MySQL Error: %s" % str(e)

def connect_DB():
    db = MySQLdb.connect(
        host = "127.0.0.1",
        user = "android",
        passwd = "12345678",
        db = "KaraokeDB")
    db.set_character_set('utf8')
    return db


if __name__=="__main__":
    app.run(host='140.116.245.248')
    
