/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2008/07/15
 *
 * This file is part of Jiemamy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.jiemamy.eclipse.core.ui;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.lang.Validate;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.seasar.eclipse.common.util.ImageManager;
import org.seasar.eclipse.common.util.StatusUtil;

import org.jiemamy.DefaultServiceLocator;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.preference.JiemamyPreference;
import org.jiemamy.eclipse.core.ui.preference.JiemamyPreferenceImpl;
import org.jiemamy.eclipse.extension.EclipseDialectServiceLocator;
import org.jiemamy.eclipse.extension.EclipseExporterServiceLocator;
import org.jiemamy.eclipse.extension.EclipseImporterServiceLocator;
import org.jiemamy.utils.CompositeServiceLocator;

/**
 * Jiemamy Eclipse Core PluginのActivatorクラス。
 * 
 * @author daisuke
 */
public class JiemamyUIPlugin extends AbstractUIPlugin {
	
	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.jiemamy.eclipse.core.ui";
	
	/** アイコンファイルの配置パス */
	private static final String ICONS_PATH = "icons/";
	
	/** プラグインクラスのシングルトンインスタンス */
	private static JiemamyUIPlugin plugin;
	
	/** 設定 */
	private static JiemamyPreference pref;
	

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static JiemamyUIPlugin getDefault() {
		assert plugin != null;
		return plugin;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path the path
	 * @return the image descriptor
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		Validate.notNull(path);
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * 設定を取得する。
	 * 
	 * @return 設定
	 */
	public static JiemamyPreference getPreference() {
		assert pref != null;
		return pref;
	}
	
	/**
	 * ログを記録する。
	 * 
	 * @param msg ログメッセージ
	 * @param intStatus ステータスコード
	 */
	public static void log(String msg, int intStatus) {
		IStatus status = StatusUtil.create(plugin, intStatus, 0, msg, null);
		plugin.getLog().log(status);
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pref = new JiemamyPreferenceImpl();
		
		// FORMAT-OFF
		JiemamyContext.setServiceLocator(new CompositeServiceLocator(
				new DefaultServiceLocator(),
				new EclipseDialectServiceLocator(),
				new EclipseImporterServiceLocator(),
				new EclipseExporterServiceLocator()
		));
		// FORMAT-ON
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		pref = null;
		plugin = null;
		super.stop(context);
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		// TODO ImageManagerを使用する。
//		Display display = Display.getCurrent();
//		if (display == null) {
//			display = new Display();
//			setupImageManager(display);
//		}
		registerImage(registry, Images.BUTTON_TABLE, "table.gif");
		registerImage(registry, Images.BUTTON_VIEW, "view.gif");
		registerImage(registry, Images.BUTTON_FK, "reference.gif");
		registerImage(registry, Images.BUTTON_INH, "reference.gif");
		
		registerImage(registry, Images.LABEL_TABLE, "circledT.gif");
		registerImage(registry, Images.LABEL_VIEW, "circledV.gif");
		
		registerImage(registry, Images.ICON_JIEMAMY, "jiemamy.gif");
		registerImage(registry, Images.ICON_TABLE, "table.gif");
		registerImage(registry, Images.ICON_VIEW, "view.gif");
		registerImage(registry, Images.ICON_COLUMN, "column.gif");
//		registerImage(registry, Images.ICON_CONTAINER, "");
		registerImage(registry, Images.ICON_DOMAIN, "circledD.gif");
		registerImage(registry, Images.ICON_PK, "primaryKey.gif");
		registerImage(registry, Images.ICON_FK, "fk.gif");
		registerImage(registry, Images.ICON_INH, "inheritance.gif");
		registerImage(registry, Images.ICON_NULL_DATA, "exclamation.gif");
		registerImage(registry, Images.ICON_COLOR_PALETTE, "palette.gif");
		
		registerImage(registry, Images.CHECK_ON, "check_on.gif");
		registerImage(registry, Images.CHECK_OFF, "check_off.gif");
		registerImage(registry, Images.CHECK_DISABLED_ON, "check_disabled_on.gif");
		registerImage(registry, Images.CHECK_DISABLED_OFF, "check_disabled_off.gif");
		registerImage(registry, Images.CHECK_HOVER_ON, "check_hover_on.gif");
		registerImage(registry, Images.CHECK_HOVER_OFF, "check_hover_off.gif");
	}
	
	/**
	 * ImageRegistryに指定したファイルの画像を設定する。
	 * 
	 * <p>画像ファイルは icons/ 内に配置する必要がある。</p>
	 * 
	 * @param registry 追加対象のImageRegistry
	 * @param key キー
	 * @param fileName ファイル名
	 */
	private void registerImage(ImageRegistry registry, String key, String fileName) {
		IPath path = new Path(ICONS_PATH + fileName);
		URL url = FileLocator.find(getBundle(), path, null);
		
		if (url != null) {
			registry.put(key, ImageDescriptor.createFromURL(url));
		}
	}
	
	/**
	 * {@link ImageManager}をセットアップする。
	 * 
	 * @param display SWTディスプレイオブジェクト
	 */
	@SuppressWarnings("unused")
	// そのうち使う予定…
	private void setupImageManager(Display display) {
		ImageManager.init(display);
		ResourceBundle imageBundle = ResourceBundle.getBundle("org.jiemamy.eclipse.images");
		ImageManager.loadImages(imageBundle);
		// TODO ImageManagerのdispose
	}
}
