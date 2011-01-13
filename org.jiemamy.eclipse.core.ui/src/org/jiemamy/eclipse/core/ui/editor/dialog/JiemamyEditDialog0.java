/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/02/24
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
package org.jiemamy.eclipse.core.ui.editor.dialog;

import java.lang.reflect.Constructor;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.JiemamyCorePlugin;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.eclipse.extension.ExtensionResolver;
import org.jiemamy.model.DiagramModel;

/**
 * Jiemamyのモデル編集ダイアログ抽象クラス。
 * 
 * @param <T> 編集対象Coreモデルの型
 * @author daisuke
 */
public abstract class JiemamyEditDialog0<T> extends Dialog {
	
	private static final String X = "x"; //$NON-NLS-1$
	
	private static final String Y = "y"; //$NON-NLS-1$
	
	private static final String WIDTH = "width"; //$NON-NLS-1$
	
	private static final String HEIGHT = "height"; //$NON-NLS-1$
	
	private IDialogSettings dialogSettings;
	
	private Point dialogLocation;
	
	private Point dialogSize;
	
	/** 編集対象モデル */
	private T targetCoreModel;
	
	/** 保持するタブのリスト */
	private List<AbstractTab> tabs = Lists.newArrayList();
	
	/** 編集対象モデルの型 */
	private final Class<?> type;
	
	/**
	 * 各コントロールに対する編集リスナ
	 * 
	 * リスナが編集を検知するたびに、OKボタンの有効化/無効化作業を行う。
	 */
	protected final EditListener editListener = new EditListenerImpl();
	
	private final JiemamyContext context;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentShell 親シェル
	 * @param context コンテキスト
	 * @param targetCoreModel 編集対象モデルの型
	 * @param type 編集対象モデルの型
	 * @throws IllegalArgumentException 引数targetModel, typeに{@code null}を与えた場合
	 */
	protected JiemamyEditDialog0(Shell parentShell, JiemamyContext context, T targetCoreModel, Class<?> type) {
		super(parentShell);
		
		Validate.notNull(context);
		Validate.notNull(targetCoreModel);
		Validate.notNull(type);
		
		this.context = context;
		this.targetCoreModel = targetCoreModel;
		this.type = type;
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DiagramModel diagramModel = facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		
		readConfiguration();
	}
	
	@Override
	public boolean close() {
		Shell shell = getShell();
		Point location = shell.getLocation();
		Point size = shell.getSize();
		boolean closed = super.close();
		if (closed) {
			writeConfiguration(size, location);
		}
		return closed;
	}
	
	/**
	 * 保持するタブのリストを取得する。
	 * 
	 * <p>このメソッドは、インスタンスの持つフィールドをそのまま返す。返される{@link List}を直接操作することで、
	 * このオブジェクトのフィールドとして保持される{@link List}を変更することができる。</p>
	 * 
	 * @return 保持するタブのリスト
	 */
	public List<AbstractTab> getTabs() {
		return tabs;
	}
	
	/**
	 * タブを追加登録する。
	 * 
	 * @param tab 追加するタブ
	 */
	protected void addTab(AbstractTab tab) {
		tabs.add(tab);
	}
	
	/**
	 * OKボタンを有効にするかどうかを調べる。
	 * 
	 * @return 有効であれば{@code true}
	 */
	protected boolean canExecuteOk() {
		// Default implementation is to check if all tabs are complete.
		for (AbstractTab tab : tabs) {
			if (tab.isTabComplete() == false) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void constrainShellSize() {
		super.constrainShellSize();
		Shell shell = getShell();
		
		if (dialogLocation != null) {
			shell.setLocation(dialogLocation);
		}
		if (dialogSize != null) {
			shell.setSize(dialogSize);
		} else {
			shell.setSize(getDefaultSize());
		}
	}
	
	/**
	 * Dialect等による追加タブを1つ作成する。
	 * 
	 * @param tabFolder 親TabFolder
	 * @param tabClassName クラス名
	 * @return 追加タブ
	 */
	protected AbstractTab createAdditionalTab(TabFolder tabFolder, String tabClassName) {
		AbstractTab tab = null;
		try {
			Class<?> tabClass = Class.forName(tabClassName);
			Constructor<?> constructor = tabClass.getConstructor(TabFolder.class, int.class, type);
			tab = (AbstractTab) constructor.newInstance(tabFolder, SWT.NULL, targetCoreModel);
		} catch (Exception e) {
			ExceptionHandler.handleException(e);
		}
		return tab;
	}
	
	/**
	 * Dialect等による追加タブをすべて作成する。
	 * 
	 * @param tabFolder 親TabFolder
	 * @return 作成した追加タブのリスト
	 */
	protected List<AbstractTab> createAdditionalTabs(TabFolder tabFolder) {
		List<AbstractTab> result = Lists.newArrayList();
		ExtensionResolver<Dialect> dialectResolver = JiemamyCorePlugin.getDialectResolver();
		
		IConfigurationElement dialectElement =
				dialectResolver.getExtensionConfigurationElements().get(context.getDialectClassName());
		
		if (dialectElement != null) {
			for (IConfigurationElement additionalTabElement : dialectElement.getChildren("additionalTab")) {
				if (additionalTabElement.getAttribute("target").equals(this.getClass().getName())) {
					String tabClassName = additionalTabElement.getAttribute("class");
					AbstractTab tab = createAdditionalTab(tabFolder, tabClassName);
					if (tab != null) {
						result.add(tab);
						addTab(tab);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * コンテキストを取得する。
	 * 
	 * @return {@link JiemamyContext}
	 */
	protected JiemamyContext getContext() {
		return context;
	}
	
	/**
	 * デフォルトのダイアログウィンドウサイズを取得する。
	 * 
	 * @return デフォルトのダイアログウィンドウサイズ
	 */
	protected abstract Point getDefaultSize();
	
	/**
	 * Returns the dialog settings object used to share state between several
	 * event detail dialogs.
	 *
	 * @return the dialog settings to be used
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = JiemamyUIPlugin.getDefault().getDialogSettings();
		dialogSettings = settings.getSection(getClass().getName());
		if (dialogSettings == null) {
			dialogSettings = settings.addNewSection(getClass().getName());
		}
		return dialogSettings;
	}
	
	/**
	 * 編集対象モデルを取得する。
	 * 
	 * @return　編集対象モデル
	 */
	protected T getTargetCoreModel() {
		return targetCoreModel;
	}
	
	@Override
	protected void okPressed() {
		if (canExecuteOk() == false) {
			return;
		}
		if (performOk()) {
			for (AbstractTab tab : tabs) {
				tab.okPressed();
			}
			super.okPressed();
		}
	}
	
	/**
	 * OKボタン押下処理を行う。
	 * Notifies that the OK button of this dialog has been pressed.
	 * 
	 * @return {@code false} to abort the container's OK
	 *  processing and {@code true} to allow the OK to happen
	 */
	protected abstract boolean performOk();
	
	/**
	 * Initializes itself from the dialog settings with the same state as at the
	 * previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();
		try {
			int x = s.getInt(X);
			int y = s.getInt(Y);
			dialogLocation = new Point(x, y);
			x = s.getInt(WIDTH);
			y = s.getInt(HEIGHT);
			dialogSize = new Point(x, y);
		} catch (NumberFormatException e) {
			dialogLocation = null;
			dialogSize = null;
		}
	}
	
	private void writeConfiguration(Point size, Point location) {
		IDialogSettings s = getDialogSettings();
		s.put(X, location.x);
		s.put(Y, location.y);
		s.put(WIDTH, size.x);
		s.put(HEIGHT, size.y);
	}
	

	/**
	 * 編集を検知するリスナ。
	 * 
	 * <p>編集が行われたタイミングで{@link JiemamyEditDialog0#canExecuteOk()}をチェックし、OKボタンを有効化・無効化する。</p>
	 * 
	 * @author daisuke
	 */
	public class EditListenerImpl extends AbstractEditListener {
		
		@Override
		protected void process(TypedEvent e) {
			getButton(IDialogConstants.OK_ID).setEnabled(canExecuteOk());
		}
	}
	
}
