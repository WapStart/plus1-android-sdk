Plus1BannerViewStateListener
============================
Интерфейс наблюдателя за состоянием видимости [Plus1BannerView](doc/Plus1BannerView.md).

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
