import tweepy
def sendTweet(webPagePath, url):
   #Collecting Title
   indexHtmlFile = open(webPagePath + "/index.html", "r")
   indexHtml = indexHtmlFile.read()
   indexHtmlFile.close()
   start = indexHtml.find("<h1>")
   end = indexHtml.find("</h1>")
   title = indexHtml[start+4:end]

   #Collect image file names
   imageFileNames = []
   imageStart = "<img src=\""
   imageEnd = "\" width"
   theString = indexHtml
   while theString.find(imageStart) != -1:
       theString = theString[theString.find(imageStart) + len(imageStart):]
       end = theString.find(imageEnd)
       imageFileName = theString[0:end]
       imageFileNames.append(imageFileName)
   
   #Tweeting
   consumer_key = 'xx'
   consumer_secret_key = 'xx'
   access_token = 'xx'
   access_token_secret = 'xx'

   auth = tweepy.OAuthHandler(consumer_key, consumer_secret_key)
   auth.set_access_token(access_token, access_token_secret)
   api = tweepy.API(auth)

   #tweet = 'new test'
   #api.update_status(tweet)
   #tweets text inside 'tweet'
   
   # Upload image
   length = len(imageFileNames)
   mediaIds = []
   if length >= 2:
       for i in range(min(3, length -1)):
           imageFileName = imageFileNames[i]
           media = api.media_upload(webPagePath + "/" + imageFileName)
           mediaIds.append(media.media_id)
       imageFileName = imageFileNames[length-1]
       media = api.media_upload(webPagePath + "/" + imageFileName)
       mediaIds.append(media.media_id)

   # Post tweet with image
   tweet = "**" + title + "**" + url
   post_result = api.update_status(status=tweet, media_ids=mediaIds)
   return str(post_result.id)

def deleteTweet(tweetId):
   #Tweeting
   consumer_key = 'xx'
   consumer_secret_key = 'xx'
   access_token = 'xx'
   access_token_secret = 'xx'

   auth = tweepy.OAuthHandler(consumer_key, consumer_secret_key)
   auth.set_access_token(access_token, access_token_secret)
   api = tweepy.API(auth)

   # Post tweet with image
   tweetId = int(tweetId)
   status = api.get_status(tweetId)
   if status:
       api.destroy_status(tweetId)

