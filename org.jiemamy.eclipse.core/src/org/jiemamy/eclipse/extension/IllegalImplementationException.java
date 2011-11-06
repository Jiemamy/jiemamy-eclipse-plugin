package org.jiemamy.eclipse.extension;

/**
 * プラグインの拡張ポイントに登録した拡張クラスが持つべきインターフェイスを持っていないことを表す例外。
 * 
 * @version $Id$
 * @author daisuke
 */
@SuppressWarnings("serial")
public class IllegalImplementationException extends RuntimeException {
	
	private final Object obj;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param obj プラグインの拡張ポイントに登録した拡張クラス
	 */
	public IllegalImplementationException(Object obj) {
		this.obj = obj;
	}
	
	/**
	 * プラグインの拡張ポイントに登録した拡張クラスを取得する。
	 * 
	 * @return プラグインの拡張ポイントに登録した拡張クラス
	 */
	public Object getObj() {
		return obj;
	}
	
}
