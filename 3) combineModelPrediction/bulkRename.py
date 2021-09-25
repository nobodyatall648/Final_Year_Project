import os

directory = "benchmark/audio/howl/"

for count, filename in enumerate(os.listdir(directory)):
        dst ="howl" + str(count) + ".wav"
        src =directory + filename
        dst = directory + dst
          
        # rename() function will
        # rename all the files
        os.rename(src, dst)