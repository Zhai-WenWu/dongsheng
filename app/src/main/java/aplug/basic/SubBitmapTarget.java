package aplug.basic;


import acore.logic.XHClick;
import acore.override.XHApplication;
import xh.basic.internet.img.BitmapTarget;

public abstract class SubBitmapTarget extends BitmapTarget {
	@Override
	public void switchDNS() {
		XHClick.statisticsSwitchDNS(XHApplication.in());
	}
}
