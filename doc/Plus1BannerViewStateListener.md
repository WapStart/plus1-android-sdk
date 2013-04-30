Plus1BannerViewStateListener
============================

**Внимание:** данный класс устарел (начиная с версии SDK 2.2.0), используйте вместо него наблюдатели описанные в документации к [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md).

```java
public interface Plus1BannerViewStateListener {
	// Вызывается при показе Plus1BannerView
	abstract public void onShowBannerView();

	// Вызывается при скрытии Plus1BannerView
	abstract public void onHideBannerView();

	// Вызывается при закрытии Plus1BannerView кнопкой
	abstract public void onCloseBannerView();
}
```
