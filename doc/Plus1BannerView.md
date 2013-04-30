Plus1BannerView
===============
Отвечает за отображение объявления. Взаимодействует с [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md).

Основные методы
------------------
* `public Plus1BannerView enableAnimationFromTop()`  
  `public Plus1BannerView enableAnimationFromBottom()`  
  Появление баннера будет анимироваться "выползанием" сверху или снизу соответственно.

* `public Plus1BannerView disableAnimation()`  
  Отключение анимации появления баннеров.

* `public Plus1BannerView addListener(* listener)`
  Добавление наблюдателя к Plus1BannerView.
  Описание наблюдателей смотрите ниже в данном документе.

* `public Plus1BannerView enableCloseButton()`  
  `public Plus1BannerView setCloseButtonEnabled(boolean closeButtonEnabled)`  
  Управление видимостью кнопки закрытия баннера. По умолчанию кнопка отображаться не будет.
  // **устарело** с версии 2.2.0: в дальнейшем оба метода будут удалены, по возможности не используйте их

* `public Plus1BannerView setAutorefreshEnabled(boolean enabled)`
  Включает или отключает автообновление баннеров. 
  // **устарело** с версии 2.2.0: используйте методы Plus1BannerAsker.start() и Plus1BannerAsker.stop() для управления обновлениями баннера

Остальные public-методы, не описанные в этом документе, используются для взаимодействия классов sdk, и могут быть использованы в отладочных целях.

Наблюдатели
-----------

```Java
  public interface OnShowListener {
    public void onShow(Plus1BannerView view);
  }
```
  Вызывется при появлении рекламного блока

```Java
  public interface OnHideListener {
    public void onHide(Plus1BannerView view);
  }
```
  Вызывется при скрытии рекламного блока

```Java
  public interface OnCloseButtonListener {
    public void onCloseButton(Plus1BannerView view);
  }
```
  Вызывется при нажатии на кнопку закрытия блока

```Java
  public interface OnExpandListener {
    public void onExpand(Plus1BannerView view);
  }
```
  Вызывется при раскрытии рекламного блока


```Java
  public interface OnCollapseListener {
    public void onCollapse(Plus1BannerView view);
  }
```
  Вызывется при сворачивании рекламного блока

```Java
  public interface OnImpressionListener {
    public void onImpression(Plus1BannerView view);
  }
```
  Вызывется при показе рекламного объявления (РО)

```Java
  public interface OnTrackClickListener {
    public void onTrackClick(Plus1BannerView view);
  }
```
  Вызывется при клике по РО
