import numpy as np
import tweepy

consumer_key = 'thH79n4IbbZGAsfhXQX63Kc5g'
consumer_secret_key = 'zHOaiPtgne7czwnZPx5ZjQa6UWQGnqMOsvCWrgmBY3DqHWBFJC'
access_token = '1419110506128834560-JEaIJM02otWW4Nn1svtoget3eN8cWH'
access_token_secret = 'n9XQA6boCm3pePBTjTxSP7oaWZieMQN7KhNxhsIgLMIIf'

auth = tweepy.OAuthHandler(consumer_key, consumer_secret_key)
auth.set_access_token(access_token, access_token_secret)
api = tweepy.API(auth)

tweet = 'test'
api.update_status(tweet)
#tweets text inside 'tweet'

tweet_text = 'test2'
image_path = './twitterpic1.png'
status = api.update_with_media(image_path, tweet_text)
#tweets image with text "test2"







