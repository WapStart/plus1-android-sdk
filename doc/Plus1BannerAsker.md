Plus1BannerAsker
================
Отвечает за получение объявлений с сервера. Взаимодействует с [Plus1BannerRequest](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerRequest.md), [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) и Activity вашего приложения.

Основные методы
------------------
* `public Plus1BannerAsker setTimeout(int timeout)`  
  Периодичность запроса объявлений в секундах (необязательный параметр, по умолчанию 10 секунд).
* `public Plus1BannerAsker setVisibilityTimeout(int visibilityTimeout)`  
  Таймаут на прекращение попыток загрузки баннера, если баннер загрузить не удаётся (необязательный параметр, по умолчанию **timeout*3**).
* `public void onPause()`  
  `public void onResume()`  
  Методы должны вызываться из соответствующих обработчиков в Activity вашего приложения.
* `public Plus1BannerAsker disableAutoDetectLocation(boolean disable)`
  Отключает/включает механизм автоматического определения местоположения пользователя через *LocationManager*.
* `public Plus1BannerAsker setViewStateListener(Plus1BannerViewStateListener viewStateListener)`  
  Устанавливает наблюдателя за состоянием видимости [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md). Подробнее см. интерфейс [Plus1BannerViewStateListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerViewStateListener.md).
* `public Plus1BannerAsker setDownloadListener(Plus1BannerDownloadListener downloadListener)`  
  Устанавливает наблюдателя загрузки объявления. Подробнее см. интерфейс [Plus1BannerDownloadListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerDownloadListener.md).
* `public void refreshBanner()`  
  Метод для обновления баннера в "ручном" режиме.

Остальные public-методы, не описанные в этом документе, используются для взаимодействия классов sdk, и могут быть использованы в отладочных целях.
