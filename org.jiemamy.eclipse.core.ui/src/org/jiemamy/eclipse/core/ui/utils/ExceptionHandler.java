/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2008/07/29
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.seasar.eclipse.common.util.LogUtil;
import org.seasar.eclipse.common.util.StatusUtil;

import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;

/**
 * 例外処理ユーティリティクラス。
 * 
 * @author daisuke
 */
public final class ExceptionHandler {
	
	/**
	 * 例外処理で、ログ出力を行う事を表す。
	 */
	public static final int LOG = 1 << 1;
	
	/**
	 * 例外処理で、ダイアログ表示を行う事を表す。
	 */
	public static final int DIALOG = 1 << 2;
	
	/**
	 * 例外処理で、標準出力にStackTraceを出力する事を表す。
	 */
	public static final int STACKTRACE = 1 << 3;
	
	/**
	 * 例外処理で、ログ出力・ダイアログ表示・StackTrace出力、全てを行う事を表す。
	 */
	public static final int ALL = LOG | DIALOG | STACKTRACE;
	
	/** ロギング対象プラグイン */
	private static Plugin plugin = JiemamyUIPlugin.getDefault();
	

	/**
	 * 例外を処理する。
	 * 
	 * @param e 例外
	 */
	public static void handleException(Throwable e) {
		handleException(e, ALL, "Exception is thrown: " + e.getClass().getCanonicalName());
	}
	
	/**
	 * 例外を処理する。
	 * 
	 * @param e 例外
	 * @param operation 例外処理の内容。下記の論理和(OR)<br>
	 *            <br>
	 *            <dl>
	 *            <dt><code>{@link #LOG}</code></dt>
	 *            <dd>ロギングを行う。</dd>
	 *            <dt><code>{@link #DIALOG}</code></dt>
	 *            <dd>ダイアログを表示する。</dd>
	 *            <dt><code>{@link #STACKTRACE}</code></dt>
	 *            <dd>StackTraceを出力する。</dd>
	 *            <dt><code>{@link #ALL}</code></dt>
	 *            <dd>上記全ての処理を行う。</dd>
	 *            </dl>
	 */
	public static void handleException(Throwable e, int operation) {
		handleException(e, operation, "Exception is thrown: " + e.getClass().getCanonicalName());
	}
	
	/**
	 * 例外を処理する。
	 * 
	 * @param e 例外
	 * @param operation 例外処理の内容。下記の論理和(OR)<br>
	 *            <br>
	 *            <dl>
	 *            <dt><code>{@link #LOG}</code></dt>
	 *            <dd>ロギングを行う。</dd>
	 *            <dt><code>{@link #DIALOG}</code></dt>
	 *            <dd>ダイアログを表示する。</dd>
	 *            <dt><code>{@link #STACKTRACE}</code></dt>
	 *            <dd>StackTraceを出力する。</dd>
	 *            <dt><code>{@link #ALL}</code></dt>
	 *            <dd>上記全ての処理を行う。</dd>
	 *            </dl>
	 * @param message メッセージ
	 */
	public static void handleException(Throwable e, int operation, String message) {
		IStatus status = StatusUtil.createInfo(plugin, Status.INFO, message, null);
		if ((operation & LOG) != 0) {
			LogUtil.log(plugin, message);
		}
		
		if ((operation & DIALOG) != 0) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			ErrorDialog.openError(window.getShell(), message, "エラーが発生しました。", // RESOURCE
					status);
		}
		
		if ((operation & STACKTRACE) != 0) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 例外を処理する。
	 * 
	 * @param e 例外
	 * @param message メッセージ
	 */
	public static void handleException(Throwable e, String message) {
		handleException(e, ALL, message);
	}
	
	private ExceptionHandler() {
	}
	
}
