/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/04/01
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
package org.jiemamy.eclipse.core.ui.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;

/**
 * Eclipseのエラーマーカー（Problemsビューに表示されるエントリ）を扱うユーティリティ。
 * 
 * @author daisuke
 */
public final class MarkerUtil {
	
	/** マーカーID */
	public static final String MARKER_ID = JiemamyUIPlugin.PLUGIN_ID + ".problem";
	

	/**
	 * 指定したリソースに対して、指定した（エラー）マーカーを作成する。
	 * 
	 * @param resource マーカー生成する対象リソース
	 * @param priority 優先順位. {@link IMarker#PRIORITY_NORMAL}等
	 * @param severity 深刻度. {@link IMarker#SEVERITY_WARNING}等
	 * @param message メッセージ
	 */
	public static void createMarker(IResource resource, int priority, int severity, String message) {
		if (resource == null) {
			return;
		}
		try {
			IMarker marker = resource.createMarker(MARKER_ID);
			Map<String, Object> attributes = new HashMap<String, Object>(3);
			attributes.put(IMarker.PRIORITY, priority);
			attributes.put(IMarker.SEVERITY, severity);
//				attributes.put(IMarker.LINE_NUMBER, line);
//				attributes.put(MARKER_ATTR_TARGET, target);
//				attributes.put(MARKER_ATTR_CATEGORY, category);
			attributes.put(IMarker.MESSAGE, message);
			marker.setAttributes(attributes);
		} catch (CoreException e) {
			ExceptionHandler.handleException(e, ExceptionHandler.ALL);
		}
	}
	
	/**
	 * ワークスペース上の全てのマーカーを削除する。
	 */
	public static void deleteAllMarkers() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		deleteMarker(root);
	}
	
	/**
	 * 指定したりソースのマーカーを全て削除する。
	 * 
	 * @param resource 対象リソース
	 */
	public static void deleteMarker(IResource resource) {
		if (resource == null) {
			return;
		}
		try {
			resource.deleteMarkers(MARKER_ID, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			ExceptionHandler.handleException(e, ExceptionHandler.ALL);
		}
	}
	
	/**
	 * 指定したリソースに紐づいているマーカを全て削除する。
	 * 
	 * @param resource 対象リソース
	 */
	public static void deleteMarkers(IResource resource) {
		for (IMarker marker : findMarker(resource)) {
			try {
				marker.delete();
			} catch (CoreException e) {
				ExceptionHandler.handleException(e, ExceptionHandler.ALL);
			}
		}
	}
	
	/**
	 * 指定したリソースに紐づいているマーカの配列を取得する。
	 * 
	 * <p>引数に{@code null}を与えた場合は、空の配列を返す。</p>
	 * 
	 * @param resource リソース
	 * @return マーカの配列. 見つからなかった場合は空の配列を返す
	 */
	public static IMarker[] findMarker(IResource resource) {
		if (resource == null) {
			return new IMarker[0];
		}
		try {
			return resource.findMarkers(MARKER_ID, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			ExceptionHandler.handleException(e, ExceptionHandler.ALL);
		}
		return new IMarker[0];
	}
	
	private MarkerUtil() {
	}
	
}
