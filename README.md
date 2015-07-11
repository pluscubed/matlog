MatLog
=========
It's like CatLog, but with material goodness.

Graphical log reader for Android.

Based on Nolan Lawson's CatLog: [Google Play][1], [GitHub][2]

Overview
---------
CatLog is a free and open-source log reader for Android.  

It shows a scrolling (tailed) view of the Android "Logcat" system log, 
hence the goofy name.  It also allows you to record logs in real time, send logs via email, 
and filter using a variety of criteria.

Download
--------------

MatLog may be downloaded from [somewhere here][4].

FAQs
-------------

#### Where are the logs saved?

On the SD card, under ```/sdcard/catlog/saved_logs/```.

#### I can't see any logs!

This problem typically shows up on custom ROMs.  First off, try an alternative logging app, to verify that
the problem is with your ROM and not CatLog.

Next, see if your ROM offers system-wide settings to disable logging.  Be sure to reboot after you change anything.

If that still doesn't work, you can contact the creator of your ROM to file a bug/RFE.

And if you don't have root, check to see if you're running Jelly Bean (Android 4.2+).  [CatLog has issues with unrooted Jelly Bean][6].


[1]: https://play.google.com/store/apps/details?id=com.nolanlawson.logcat
[2]: https://github.com/nolanlawson/Catlog
