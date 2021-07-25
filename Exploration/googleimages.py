from serpapi import GoogleSearch # pip install google-search-results
import json

# with open("api_key.txt", "r") as f:
#     key = f.read()

# params = {
#     "api_key": key,
#     "engine": "google_reverse_image",
#     "google_domain": "google.com",
#     "image_url": "https://i.imgur.com/sSF5bdA.jpeg"
# }

# search = GoogleSearch(params)
# results = search.get_dict()

# with open("results.txt", "w") as f:
#     f.write(json.dumps(results))


with open("results.txt", "r") as f:
    results = json.loads(f.read())

print(json.dumps(results, indent=4))

