Plus1BannerDownloadListener 
===========================
Интерфейс наблюдателя загрузки баннера.

```java
public interface Plus1BannerDownloadListener {
	public static enum LoadError {
		UnknownAnswer,
		DownloadFailed,
		NoHaveBanner
	}

	// Вызывается в случае, когда объявление успешно загружено
	public void onBannerLoaded();

	// Вызывается в случае проблем с загрузкой объявления
	public void onBannerLoadFailed(LoadError error);
}
```
