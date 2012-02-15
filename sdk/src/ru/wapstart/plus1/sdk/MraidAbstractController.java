package ru.wapstart.plus1.sdk;

class MraidAbstractController {
	private final MraidView mView;

	MraidAbstractController(MraidView view) {
		super();
		mView = view;
	}

	public MraidView getView() {
		return mView;
	}
}
