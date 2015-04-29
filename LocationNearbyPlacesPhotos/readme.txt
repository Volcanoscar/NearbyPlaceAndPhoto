Project : Nearby places photos
API used : Google place api, google place photo api

Detailed working of app :
1. A page with search bar on top, where you can search nearby places within radius of 50000 mt.
2. place name and image will display in list, its infinite list, initially 20 items display, when scroll it grows.
3. make sure internet is working and GPS is enabled in phone.
4. on click of place name, a page with complete image open, it will support zoom in/out feature.
5. all images loaded will be cache in phone memery, you can view them without internet connection.

Some limitation :
Since google api request has limit, i restricted only max 50 result for one search in infinite listview.

Classes details :
MainActivity : Launched first, if any image already cached then it will display in list. other wise when you search online content will be visible.
PhotoDetailActivity : display any image in full view mode with zoom in/out support.
InfiteScrollListener :  to support endless scrolling. added this listener in mainactivity listview.

PlaceJsonParsor : parse data coming from place api.
PhotoWithPlace : contains place and photo reference object.

TouchImageView : Custom view to support zoom in/out support.
ImageLoadingAdapter : adapter class for loading binding PlaceWithPhoto data with listView.
OfflineLoadingAdapter : support offline loading of cached bitmap. only done caching of bitmap, data is different from online content. only image bitmap is there during offline loading.

ImageLoader : support asynchronus loading of images.
MemoryCache : cache for already loaded image.
FileCache : To interact with file system.

