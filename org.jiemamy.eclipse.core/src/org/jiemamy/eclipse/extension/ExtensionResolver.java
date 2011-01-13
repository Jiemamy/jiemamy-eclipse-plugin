/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2008/08/03
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
package org.jiemamy.eclipse.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.seasar.eclipse.common.util.ExtensionAcceptor;

import org.jiemamy.eclipse.JiemamyCorePlugin;

/**
 * 拡張ポイントに設定された項目の読み出しを行うクラス。
 * 
 * @param <T> 拡張クラスが実装すべきインターフェイス
 * @author daisuke
 */
public class ExtensionResolver<T> {
	
	/** 拡張のMap */
	private Map<String, IConfigurationElement> extensionConfigurationElements;
	
	private List<T> allInstance;
	

	/**
	 * リゾルバを初期化する。 以前読み込んだ情報を全て破棄し、拡張ポイントを読み込み直す。
	 * 
	 * <p>識別子には id 属性を使用する。</p>
	 * 
	 * @param pluginId プラグインID
	 * @param extensionPointName 拡張ポイント名
	 * @param extensionElementName 要素名
	 */
	public ExtensionResolver(String pluginId, String extensionPointName, final String extensionElementName) {
		this(pluginId, extensionPointName, extensionElementName, "id");
	}
	
	/**
	 * リゾルバを初期化する。 以前読み込んだ情報を全て破棄し、拡張ポイントを読み込み直す。
	 * 
	 * @param pluginId プラグインID
	 * @param extensionPointName 拡張ポイント名
	 * @param extensionElementName 要素名
	 * @param identifierAttributeName 識別子として扱う属性名
	 */
	public ExtensionResolver(String pluginId, String extensionPointName, final String extensionElementName,
			final String identifierAttributeName) {
		extensionConfigurationElements = Maps.newHashMap();
		
		ExtensionAcceptor.accept(pluginId, extensionPointName, new ExtensionAcceptor.ExtensionVisitor() {
			
			public void visit(IConfigurationElement element) {
				if (extensionElementName.equals(element.getName())) {
					extensionConfigurationElements.put(element.getAttribute(identifierAttributeName), element);
				}
			}
		});
	}
	
	/**
	 * 拡張ポイントのclass属性に設定された全ての実装クラスのインスタンスをリストで取得する。
	 * 
	 * @return 実装クラスのインスタンスリスト
	 */
	public List<T> getAllInstance() {
		return getAllInstance("class");
	}
	
	/**
	 * 拡張ポイントに設定された全ての実装クラスのインスタンスをリストで取得する。
	 * 
	 * @param classAttributeName 拡張ポイント定義の属性名
	 * @return 実装クラスのインスタンスリスト
	 */
	public List<T> getAllInstance(String classAttributeName) {
		if (allInstance == null) {
			List<T> result = new ArrayList<T>();
			for (String key : extensionConfigurationElements.keySet()) {
				try {
					result.add(getInstance(key, classAttributeName));
				} catch (CoreException e) {
					Plugin plugin = JiemamyCorePlugin.getDefault();
					String symbolicName = plugin.getBundle().getSymbolicName();
					IStatus status = new Status(IStatus.ERROR, symbolicName, 0, "extension instantiation error.", e);
					ILog log = plugin.getLog();
					log.log(status);
				}
			}
			
			allInstance = ImmutableList.copyOf(result);
		}
		return allInstance;
	}
	
	/**
	 * 拡張のMapを取得する。
	 * 
	 * @return フィルタのファクトリ
	 */
	public Map<String, IConfigurationElement> getExtensionConfigurationElements() {
		return extensionConfigurationElements;
	}
	
	/**
	 * 拡張定義のclass属性に設定されたクラスのインスタンスを取得する。
	 * 
	 * @param identifier 拡張識別子
	 * @return インスタンス
	 * @throws CoreException if an instance of the executable extension could not be created for any reason
	 */
	public T getInstance(String identifier) throws CoreException {
		return getInstance(identifier, "class");
	}
	
	/**
	 * 拡張定義に設定されたクラスのインスタンスを取得する。
	 * 
	 * @param identifier 拡張識別子
	 * @param classAttributeName クラス名が記述されている属性名
	 * @return インスタンス
	 * @throws CoreException if an instance of the executable extension could not be created for any reason
	 */
	public T getInstance(String identifier, String classAttributeName) throws CoreException {
		Object obj = extensionConfigurationElements.get(identifier).createExecutableExtension(classAttributeName);
		T instance;
		try {
			@SuppressWarnings("unchecked")
			// キャスト失敗してもcatchするので、OK
			T t = (T) obj;
			instance = t;
		} catch (ClassCastException e) {
			throw new IllegalImplementationException(obj);
		}
		return instance;
	}
}
