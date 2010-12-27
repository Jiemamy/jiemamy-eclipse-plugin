/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/02/17
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
package org.jiemamy.eclipse.editor.dialog.sticky;

import org.apache.commons.lang.Validate;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.Images;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.ui.JiemamyEditDialog;
import org.jiemamy.eclipse.ui.tab.TextEditTab;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.StickyNodeModel;

/**
 * Sticky設定ダイアログクラス。
 * 
 * @author daisuke
 */
public class StickyEditDialog extends JiemamyEditDialog<StickyNodeModel> {
	
	private static final Point DEFAULT_SIZE = new Point((int) (370 * 1.618), 370);
	
	private TextEditTab tabContents;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private final int diagramIndex;
	
	/** モデル操作に用いるファサード */
	private final JiemamyViewFacade jiemamyFacade;
	

	/**
	 * コンストラクタ。
	 * 
	 * @param shell 親シェルオブジェクト
	 * @param stickyModel 編集対象付箋モデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param jiemamyFacade モデル操作に用いるファサード
	 * @throws IllegalArgumentException 引数stickyModel, jiemamyFacadeに{@code null}を与えた場合
	 */
	public StickyEditDialog(Shell shell, JiemamyContext context, StickyNodeModel stickyModel, int diagramIndex,
			JiemamyViewFacade jiemamyFacade) {
		super(shell, context, stickyModel, StickyNodeModel.class);
		
		Validate.notNull(stickyModel);
		Validate.notNull(jiemamyFacade);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.diagramIndex = diagramIndex;
		this.jiemamyFacade = jiemamyFacade;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final StickyNodeModel stickyModel = getTargetModel();
		getShell().setText(Messages.Dialog_Title);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(6, false));
		
		// ---- A-1. 色
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		
		Button btnColor = new Button(composite, SWT.PUSH);
		btnColor.setImage(ir.get(Images.ICON_COLOR_PALETTE));
		btnColor.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				ColorDialog colorDialog = new ColorDialog(getShell(), SWT.NULL);
				RGB rgb = colorDialog.open();
				if (rgb != null) {
					jiemamyFacade.setColor(diagramIndex, stickyModel, ConvertUtil.convert(rgb));
				}
			}
		});
		
		Button btnDefaultColor = new Button(composite, SWT.PUSH);
		btnDefaultColor.setText("default color"); // RESOURCE
		btnDefaultColor.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				jiemamyFacade.setColor(diagramIndex, stickyModel, null);
			}
		});
		
		// ---- B. タブ
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 6;
		tabFolder.setLayoutData(gd);
		
		// ---- B-1. Contents
		String contents = JiemamyPropertyUtil.careNull(stickyModel.getContents());
		tabContents = new TextEditTab(tabFolder, Messages.Tab_Sticky_Contents, contents);
		tabContents.addKeyListener(new EditListenerImpl());
		addTab(tabContents);
		
		createAdditionalTabs(tabFolder);
		
		return composite;
	}
	
	@Override
	protected Point getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	@Override
	protected boolean performOk() {
		StickyNodeModel stickyModel = getTargetModel();
		
		String contents = JiemamyPropertyUtil.careNull(tabContents.getTextWidget().getText(), true);
		jiemamyFacade.changeModelProperty(stickyModel, StickyProperty.contents, contents);
		
		return true;
	}
}
