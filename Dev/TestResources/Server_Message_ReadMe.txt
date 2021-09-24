xxxx.bin files have the messages that TwitterHelper APP sends to apps4sj.org via port 32421 to control the ads on apps4sj.org

The common structures are:

1. Total byte length of the whole message (including the 10byte length info)in text (10bytes)
2. A json structure that defines the message
3. One charactor of "LF"(aka new line).
4. May or may not have sequentially concatenated binary images.

Types of messages:

stage:
    For both creating a new ads or updating existing ads
    After received by the server, a new webpage is created and staged, but not published yet. A jpeg of the webpage preview is returned.
    Word "Error" is returned if something is wrong.
    
publish:
    To bring a staged ad to publish, the aging starts from the current time. If there is an existing ad that has the same ID, the existing ad is replaced by the new one, and the aging restarts from the current time.
    The url of the ads is returned if everything is OK.
    Word "Error" is returned is something is wrong.

delete:
    For deleting staged and published ads.
    Word "deleted" is returned if everything is fine.
    Word "Error" is returned is something is wrong.

extend:
    To extend expiration time for the published ads. The aging restarts from the current time.
    Word "extended" is returned if everything is fine.
    Word "Error" is returned is something is wrong.

clean:
    For deleting all published and staged expired ads.
    Word "extended" is returned if everything is good.
    Word "Error" is returned is something is wrong.
