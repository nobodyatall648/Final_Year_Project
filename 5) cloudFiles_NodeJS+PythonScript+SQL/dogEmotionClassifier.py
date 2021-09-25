import cv2
import librosa
#suppress tensorflow errors
import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 

#from keras.models import load_model
from tensorflow import keras
import os
import numpy as np
import sys
import random

if(len(sys.argv) != 3):
    print("[*] format => ./python dogEmotionClassifier.py <image location> <audio location>")
    sys.exit()

#load models & define variables
dogBarkLoadedmodel = keras.models.load_model('/home/cloudsystester/cloudFiles/model/dog_barks_classification.hdf5')
dogEmotionLoadedmodel = keras.models.load_model('/home/cloudsystester/cloudFiles/model/dog_emotion_recognition.hdf5')
imageFile = sys.argv[1]
audioFile = sys.argv[2]
labels = ['happy', 'angry', 'sick']

#dogBarkLoadedmodel = './model/%s' % (dogBarkModel)
#dogEmotionLoadedmodel = './model/%s' % (dogEmotionModel)

#defining functions
def convert(fileAudio):
   
    #original file in dogBarks downloaded are m4a file format
    #start converting m4a => wav
    outFile = "%s_conv.wav" % (random.randint(1, 99999999))
    cmd = """ffmpeg -i %s -acodec pcm_u8 -ac 1 %s%s >/dev/null 2>&1""" % (fileAudio, "/home/cloudsystester/cloudFiles/converted/", outFile)
    os.system(cmd)
    return outFile

def dogEmotionPred(filepath):
    img_rows = 224
    img_cols = 224
    
    img = cv2.imread(filepath)
    img = cv2.cvtColor(img,cv2.COLOR_BGR2RGB)
    
    img = cv2.resize(img,(img_rows,img_cols)).astype('float32')
    img /= 255
    img = np.expand_dims(img,axis=0)
    predict_label = dogEmotionLoadedmodel.predict(img)
    
    return predict_label

def dogBarkPred(filepath):
    audio, sample_rate = librosa.load(filepath, res_type='kaiser_fast') 
    mfccs_features = librosa.feature.mfcc(y=audio, sr=sample_rate, n_mfcc=40)
    mfccs_scaled_features = np.mean(mfccs_features.T,axis=0) #scaling
    data = mfccs_scaled_features.reshape(1,-1)
    
    #predicted_label = chosen_model.predict_classes(data)
    predicted_label = dogBarkLoadedmodel.predict(data)
    
    return predicted_label

def average(a, b):
    combinedList = []
    combinedList.append(a.tolist())
    combinedList.append(b.tolist())
    
    dogPredArr = np.array(combinedList)
    predAvg = np.average(dogPredArr, axis=0, weights=[7,3])
    
    return predAvg

#start predicting
imgFile = os.path.join(imageFile)
barkFile = os.path.join(audioFile)

convAudioFile = convert(barkFile)

dogEmotionPredRsl = dogEmotionPred(imgFile)
dogBarkPredRsl = dogBarkPred("%s/%s" % ("/home/cloudsystester/cloudFiles/converted", convAudioFile))
dogPredAvg = average(dogEmotionPredRsl[0], dogBarkPredRsl[0])
predict_label = np.argmax(dogPredAvg, axis=0)

#print dog emotion predicted result to console to return back to node.js
print('{}'.format(labels[predict_label]))


