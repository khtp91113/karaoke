# Karaoke

## Topic

設計一個卡拉ok軟體，用戶能連上server選擇自己想唱的歌曲，開心的唱 ε٩(๑> ₃ <)۶з<br/> 
還能分享引以為豪的錄音檔給其他用戶聆聽 (*´∀`)~♥ <br/>
*~~(用戶們自己要小心"胖虎"( ´థ౪థ）σ)~~* <br/>
>  This app is like a box of chocolates. You never know what you're gonna listen.  


##### 註:2019 Android Class Final Project

## APK Link
[APK](https://github.com/khtp91113/karaoke/blob/master/release/app-release.apk)

## Libraray
- [SoundTouch](https://gitlab.com/soundtouch/soundtouch)
    - 音訊編輯C++ library，提供調整pitch、播放速度等功能
    - 本專題用於調整音訊的pitch，達到升降key功能
    - [Install Reference](https://www.jianshu.com/p/41e0e39031bb)
- [Apache Net](https://commons.apache.org/proper/commons-net/)
    - 提供網路連線相關功能
    - 本專題用於和FTPS server建立連線、抓取音訊檔

## Platform
Android Studio 3.3

## Device
### 虛擬機

### 實體機: HTC U11+ 
- Android 版本:  8.0.0
- API: 26

## Server建置
### VM
- Ubuntu 18.04.2 LTS (Bionic Beaver)
    - Memory:5100MB
    - Disk:10GB
    - 安裝套件:
        - MySQL
        - vsftpd
        - python-flask
        
### MySQL
- 作為整個專案儲存資料地方
- 主要使用三張表格
    - KaraokeUsers
        - 存放使用者資料、帳號與密碼等設定
        - 未來會將密碼做hash來增強安全性
    - PersonalMusic
        - 提供個人錄製音檔建檔與檔案連結
        - 搭配FTP來達成雲端存放功能
    - PublicMusic
        - 提供可以演唱的歌曲
        - 包含歌曲相關資訊
        - 使用資料庫讓搜尋更加便利
        - 搭配FTP來達成手機端的音樂載入

###### KaraokeUsers Table

| Field|Type | Null | Key | Default | Extra |
| --- | --- |--- |--- |--- |--- |
| UID | int(11)     | NO   | PRI | NULL              | auto_increment              |
| Username | varchar(20) | NO   | UNI | NULL              |                             |
| Passwd   | varchar(20) | NO   |     | NULL              |                             |
| ModTime  | timestamp   | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |

###### PersonalMusic Table

| Field             | Type         | Null | Key | Default           | Extra                       |
|-------------------|--------------|------|-----|-------------------|-----------------------------|
| PMID              | int(11)      | NO   | PRI | NULL              | auto_increment              |
| PersonalMusicName | varchar(255) | NO   | MUL | NULL              |                             |
| Path              | varchar(255) | NO   |     | NULL              |                             |
| Timestamp         | timestamp    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
| UID               | int(11)      | NO   |     | NULL              |                             |
| MID               | int(11)      | NO   |     | NULL              |                             |

###### PublicMusic Table


| Field     | Type         | Null | Key | Default | Extra          |
|-----------|--------------|------|-----|---------|----------------|
| MID       | int(11)      | NO   | PRI | NULL    | auto_increment |
| MusicName | varchar(255) | NO   |     | NULL    |                |
| Path      | varchar(255) | NO   |     | NULL    |                |
| Path2     | varchar(255) | YES  |     | NULL    |                |
| Path3     | varchar(255) | YES  |     | NULL    |                |
| Artist    | varchar(20)  | YES  |     | NULL    |                |
| Gender    | varchar(10)  | YES  |     | NULL    |                |
| Language  | varchar(10)  | YES  |     | NULL    |                |
| Count     | int(11)      | YES  |     | NULL    |                |

##### vsftpd
- 專案儲存音檔地方
- 主要搭配資料庫來達成雲端建置
- 有助於減輕手機端的使用空間，龐大的音樂資料中找尋所需音樂再進行下載
- 使用TLS加密來增強其傳輸安全性
##### python-flask
- 主要為串接MySQL資料庫接口
- 提供常用資料庫新增、修改、刪除指令封裝成Restful API方便手機端程式連接
- 連結Server為https://serverIP:port/Function

###### 可使用的API
|Function|輸入參數|輸出參數|功能介紹|
|---|---|---|---|
|Login |username,passwd|UID|UID為-1時登入失敗|
|CreateAccount|username,passwd|UID|UID作為使用者獨一無二編號|
|ChangePasswd|UID,oldpw,newpw|0:失敗,<br>1:成功|更改密碼|
|QuerySongListByArtist|Artist|Table of PublicMusic|以演唱者來搜尋相關音樂資訊|
|QuerySongListByGender|Gender|Table of PublicMusic|以演唱者性別或團體來搜尋相關音樂資訊|
|QuerySongListByLanguage|Language|Table of PublicMusic|以演唱語言來搜尋相關音樂資訊|
|QuerySongListByCount|Count|Table of PublicMusic|以點播率來搜尋相關音樂資訊|
|check|NULL|Table of PublicMusic|Debug用途:dump所有音樂|
|QuerySong|MusicName|Only one row of table of PublicMusic|指定歌曲資訊|
|UploadPersonalSong|PersonalMusicName,UID,MusicName|Path|上傳個人錄製音樂資訊|
|QueryPersonalSong|UID,PersonalMusicName|Path|獲取個人錄製音樂資訊檔案位置|
|QueryAllRecords|NULL|Table of PersonalMusic|Debug用途:dump所有音樂|
|RemovePersonalSong|UID,PersonalMusicName|0:失敗,<br>1:成功|移除個人錄製音樂|
|QueryPersonalSongList|UID|Table of PersonalMusic|獲取個人錄製音樂列表|


## 程式畫面(功能)
### 主頁面
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/before_login.png)
* 提供用戶以語言、歌手性別、熱門等分類的音樂
* 畫面右上有用戶介面
    * 登入前人頭為黑色、登入後會變綠色
* 畫面正下方能選擇是要唱歌還是聽別人存在Server上的錄音檔

### 聽其他人的錄音頁面
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/all_record.png)
* 以上傳時間排序，可以看到是哪個用戶唱的什麼歌

### 登入頁面
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/login.png)
* 用戶未登入前點擊人頭會展示此頁面，用戶可以在此登入或註冊新帳號

### 用戶資訊
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/user_detail.png)
* 用戶登入後點擊人頭會展示用戶資訊
* Check upload會列出所有用戶曾上傳過的錄音檔

### 個人錄音檔
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/show_personal_records.png)
* 用戶可以播放自己的錄音檔
* 系統會在本地端檢查錄音檔和歌詞是否還在，若否再連FTPS Server下載
* 用戶可以刪除錄音檔(本地端、server都會刪除)

### 歌曲列表
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/song_list.png)
* 在主畫面選擇分類後會列出所有符合分類的歌，依序為歌名、歌手、唱歌次數

### 唱歌頁面
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/sing_page.png)

* 在剛進唱歌頁面時會檢查網路是連WIFI或是其他網路(4G)，若是其他網路會跳出詢問視窗確認是否要下載音訊檔
* 使用動態歌詞，讓用戶能邊看邊唱，不怕忘詞
* 設置多個功能按鈕，如切換原唱/去人聲版音樂、升降半音調、重唱、停止錄音等，可以長壓按鈕看該按鈕的功能

### 錄音設置頁面
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/save_record.png)
* 錄音停止後會進到本頁面，用戶可以在這對錄音檔和音樂的聲音大小做調整後再存檔
* 用戶可以將錄音檔上傳到Server，分享給其他用戶
    * 未登入用戶沒有上傳功能

### 聽錄音檔頁面
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/listen_record.png)
* 在個人錄音檔或聽其他人的錄音檔頁面選擇音訊後，都會開啟此頁面播放
* 播放錄音的同時，提供動態歌詞觀看

## Flow Chart
![](https://github.com/khtp91113/karaoke/blob/master/screenshots/flowChart.png)


## Future work
1. 加入評分機制
    - 可以加入如音準(使用short FFT偵測人聲頻率)、咬字等評分項目
2. 讓用戶能使用本地端的音樂，在手機上做人聲分離
    - 目前有做出利用android內建的MediaCodec將mp3解碼成PCM，將右聲道聲波反轉加上左聲道聲波達到簡易的去人聲功能
    - 缺點: 一首4分多鐘音樂，需要花個30~40秒在解碼上
3. 加入歌曲搜尋、多重篩選功能


## Contribution
- 勞宏斌(25%):負責主頁面、用戶資訊頁面、登入註冊頁面、歌曲列表頁面 & 用戶資訊設定
- 王俞婷(25%):負責動態歌詞抓取和顯示、設計唱歌頁面UI、程式美化(字體、圖片)
- 方鈞麒(25%):負責音訊處理和歌唱頁面功能(eg. 錄音、升降key、音量比例調整...)
- 陳俊佑(25%):負責錄音檔頁面、架伺服器(MySQL、FTPS server)，設計server和手機端溝通的API
