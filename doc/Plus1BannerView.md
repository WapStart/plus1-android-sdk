Plus1BannerView
===============
Отвечает за отображение объявления. Взаимодействует с [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md).

Основные методы
------------------
* `public Plus1BannerView enableCloseButton()`  
  `public Plus1BannerView setCloseButtonEnabled(boolean closeButtonEnabled)`  
  Управление видимостью кнопки закрытия баннера. По умолчанию кнопка отображаться не будет.
* `public Plus1BannerView enableAnimationFromTop()`  
  `public Plus1BannerView enableAnimationFromBottom()`  
  Появление баннера будет анимироваться "выползанием" сверху или снизу соответственно.
* `public Plus1BannerView disableAnimation()`  
  Отключение анимации появления баннеров.
* `public Plus1BannerView setAutorefreshEnabled(boolean enabled)`  
  Включает или отключает автообновление баннеров.

Остальные public-методы, не описанные в этом документе, используются для взаимодействия классов sdk, и могут быть использованы в отладочных целях.
