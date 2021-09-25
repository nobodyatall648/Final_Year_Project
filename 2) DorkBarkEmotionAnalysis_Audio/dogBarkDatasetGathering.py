#!/usr/bin/python3

'''
Details
=======
python version: 3.8.8
'''
import youtube_dl
import datetime
import pandas as pd

#for youtube-dl logging & hooking message
class Logger(object):
    def debug(self, msg):
        pass

    def warning(self, msg):
        pass

    def error(self, msg):
        print("[!] ERROR: " + msg)

def hook(d):
    if d['status'] == 'finished':
        print('[*] Done downloading, converting...')

'''
dataset class label meaning:
============================
/m/0ghcn6 => growling   #angry dog
/m/07qf0zm => howl      #get ur dog to the vet (sick dog)
/m/07rc7d9 => Bow-wow   #happy/communicating dog (happy dog)
'''

#download youtube videos
def downloadAudio(YTID, startTime, endTime, clabel, dlCount):
    #init param
    #youtube video URL
    videoID = YTID
    videoURL = "https://www.youtube.com/embed/" + videoID
        
    #output directory
    dataDir = "dogBarks"        
    filename = "%s_%d.wav" % (clabel,dlCount)

    #convert time range (second) => hh:mm:ss format (for ffmpeg postprocessing)
    startTime = str(datetime.timedelta(seconds=startTime))
    endTime = str(datetime.timedelta(seconds=endTime))

    #youtube-dl param setting
    #reference: youtube-dl options => https://github.com/ytdl-org/youtube-dl/blob/master/youtube_dl/YoutubeDL.py
    ydl_opts = {
        'format': 'bestaudio/best',    
        'outtmpl': """%s/%s/%s""" % (dataDir,clabel,filename), #save output file name in specific labelled directory             
        'postprocessors': [{
            'key': 'FFmpegExtractAudio',
            'preferredcodec': 'wav',
            'preferredquality': '192'        
        }],    
        'postprocessor_args': [ #FFMPEG arg: getting the dog bark audio based on the dataset startTime - endTime.
            "-ss", startTime,
            "-t",endTime 
        ],
        'logger': Logger(),
        'progress_hooks': [hook],
    }

    #Download videos with youtube-dl
    with youtube_dl.YoutubeDL(ydl_opts) as ydl:
        try:
            print("[*] Start downloading => %s, %s" % (clabel, videoURL))
            ydl.download([videoURL])
        except:
            print("[!] Download Failed: There's some problem with the video %s" % (YTID))

#search rows from dataset based on class label
def searchClassLabel(dataset, classLabel):    
    #mapping of class label to original dataset label
    ClassLabelMapping = {
        'growling': '/m/0ghcn6',        
        'howl': '/m/07qf0zm',
        'bow-wow': '/m/07rc7d9'
    }
    if(classLabel in ClassLabelMapping.keys()):
        colNames = ['YTID', 'startSec', 'endSec', 'positiveLabels']
        csvRead = pd.read_csv(dataset, comment='#', sep=', ', names=colNames, engine='python')
        rowsRetrieved = csvRead.loc[csvRead['positiveLabels'].str.contains(ClassLabelMapping[classLabel])]

        return rowsRetrieved
    else:
        print("[!] ERROR: The Key %s Not Found" % (classLabel))

#downloading bulk size of audios
def downloadBulkAudios(classLabelData, clabel):        
    #download each video from pandas DataFrame     
    count = 0
    for row_index, row in classLabelData.iterrows():
        downloadAudio(row['YTID'], row['startSec'], row['endSec'], clabel, count)
        count+=1

#main function
def main():    
    #init params    
    clabel = ['bow-wow', 'growling', 'howl']
    dataset = './dataset/balanced_train_segments.csv'

    for label in clabel:        
        print("[*] Start Downloading: %s audios." % (label), end="\n\n")
        foundClassData = searchClassLabel(dataset, label)
        downloadBulkAudios(foundClassData, label)
        print("[*] Download Finished: %s audios." % (label), end="\n\n")
        
if __name__ == "__main__":
    main()
    




