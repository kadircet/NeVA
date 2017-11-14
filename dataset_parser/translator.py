import urllib
import urllib2

api_key = "trnsl.1.1.20171114T121819Z.260cc24adf90daa9.d0f6b72d715f1ec6369a4ff855eb257aeaab16d6"
url = "https://translate.yandex.net/api/v1.5/tr.json/translate"

def translate( text ):
    data = {
        "lang" : "en-tr",
        "text" : text,
        "key" : api_key,
        "format" : "plain" 
    }

    data = urllib.urlencode(data)
    request = urllib2.Request(url, data)

    response = urllib2.urlopen(request)

    return response.read()