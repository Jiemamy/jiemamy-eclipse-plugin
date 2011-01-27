/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/21
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
package org.jiemamy.eclipse.core.ui.editor.dialog.foreignkey;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.editor.dialog.JiemamyEditDialog0;
import org.jiemamy.eclipse.core.ui.utils.KeyConstraintUtil;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.ModelConsistencyException;
import org.jiemamy.model.column.ColumnModel;
import org.jiemamy.model.constraint.DefaultDeferrabilityModel;
import org.jiemamy.model.constraint.DefaultForeignKeyConstraintModel;
import org.jiemamy.model.constraint.DeferrabilityModel;
import org.jiemamy.model.constraint.DeferrabilityModel.InitiallyCheckTime;
import org.jiemamy.model.constraint.ForeignKeyConstraintModel;
import org.jiemamy.model.constraint.ForeignKeyConstraintModel.MatchType;
import org.jiemamy.model.constraint.ForeignKeyConstraintModel.ReferentialAction;
import org.jiemamy.model.constraint.KeyConstraintModel;
import org.jiemamy.model.constraint.LocalKeyConstraintModel;
import org.jiemamy.model.table.DefaultTableModel;
import org.jiemamy.model.table.TableModel;
import org.jiemamy.utils.LogMarker;

/**
 * 外部キー設定ダイアログクラス。
 * 
 * @author daisuke
 */
public class ForeignKeyEditDialog extends JiemamyEditDialog0<DefaultForeignKeyConstraintModel> {
	
	private static Logger logger = LoggerFactory.getLogger(ForeignKeyEditDialog.class);
	
	/** ダイアログのデフォルトサイズ */
	private static final Point DEFAULT_SIZE = new Point((int) (250 * 1.618), 250);
	
	/** 制約を受けるテーブルのカラムのリスト */
	private List<ColumnModel> sourceColumns;
	
	/** 参照キー（制約をするテーブルのキー）の候補リスト */
	private List<LocalKeyConstraintModel> referenceKeys;
	
	/** 制約を受けるカラムを選択するコンボボックスのリスト */
	private List<Combo> keyColumnCombos;
	
	/** 制約するカラムを表示するラベルのリスト */
	private List<Label> referenceColumnLabels;
	
	/** キー名入力テキストエリア */
	private Text txtKeyName;
	
	/** 参照キー（制約をするテーブルのキー）を選択するコンボボックス */
	private Combo cmbReferenceKey;
	
	/** 遅延可能チェックボックス */
	private Button chkDeferrable;
	
	/** 即時評価ラジオボタン */
	private Button radImmediate;
	
	/** 遅延評価ラジオボタン */
	private Button radDeferred;
	
	/** マッチ型選択コンボボックス */
	private Combo cmbMatchType;
	
	/** ON DELETE選択コンボボックス */
	private Combo cmbOnDelete;
	
	/** ON UPDATE選択コンボボックス */
	private Combo cmbOnUpdate;
	
	/** マッピング設定エリアグループ */
	private Group grpMapping;
	
	/** ダイアログエリア全体 */
	private Composite dialogArea;
	

	/**
	 * コンストラクタ。
	 * 
	 * @param shell 親シェルオブジェクト
	 * @param context コンテキスト
	 * @param foreignKey 編集対象外部キー
	 * @throws IllegalArgumentException 引数foreignKey, jiemamyFacadeに{@code null}を与えた場合
	 * @throws ModelConsistencyException
	 */
	public ForeignKeyEditDialog(Shell shell, JiemamyContext context, DefaultForeignKeyConstraintModel foreignKey) {
		super(shell, context, foreignKey, DefaultForeignKeyConstraintModel.class);
		
		Validate.notNull(foreignKey);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		TableModel sourceTableModel = foreignKey.findDeclaringTable(context.getTables());
		if (sourceTableModel == null) {
			throw new ModelConsistencyException("source table not found");
		}
		
		TableModel targetTableModel =
				(TableModel) DefaultTableModel.findReferencedDatabaseObject(context.getTables(), foreignKey);
		if (targetTableModel == null) {
			throw new ModelConsistencyException("target table not found");
		}
		
		sourceColumns = sourceTableModel.getColumns();
		referenceKeys = Lists.newArrayList(targetTableModel.getConstraints(LocalKeyConstraintModel.class));
		
		keyColumnCombos = Lists.newArrayListWithCapacity(referenceKeys.size());
		referenceColumnLabels = Lists.newArrayListWithCapacity(referenceKeys.size());
		logger.debug(LogMarker.LIFECYCLE, "construct");
	}
	
	@Override
	public int open() {
		logger.debug(LogMarker.LIFECYCLE, "open");
		ForeignKeyConstraintModel foreignKey = getTargetCoreModel();
		
		// 本来 super.open() 内でコントロール生成が行われるのだが、事前に値をセットする為に、このタイミングでコントロール生成を行う。
		// ここで生成を行ってしまっても、二度コントロール生成されることはない。 {@link org.eclipse.jface.window.Window#open()}の実装を参照。
		create();
		
		logger.debug(LogMarker.LIFECYCLE, "set current value to controls");
		txtKeyName.setText(StringUtils.defaultString(foreignKey.getName()));
		
		MatchType matchType = foreignKey.getMatchType();
		cmbMatchType.setText(matchType == null ? StringUtils.EMPTY : matchType.name());
		
		ReferentialAction onDelete = foreignKey.getOnDelete();
		cmbOnDelete.setText(onDelete == null ? StringUtils.EMPTY : onDelete.name());
		
		ReferentialAction onUpdate = foreignKey.getOnUpdate();
		cmbOnUpdate.setText(onUpdate == null ? StringUtils.EMPTY : onUpdate.name());
		
		DeferrabilityModel deferrability = foreignKey.getDeferrability();
		
		if (deferrability == null) {
			radImmediate.setSelection(true);
			radDeferred.setSelection(false);
		} else {
			radImmediate.setSelection(deferrability.getInitiallyCheckTime() == InitiallyCheckTime.IMMEDIATE);
			radDeferred.setSelection(deferrability.getInitiallyCheckTime() == InitiallyCheckTime.DEFERRED);
		}
		
		if (deferrability != null && deferrability.isDeferrable()) {
			chkDeferrable.setSelection(true);
			radImmediate.setEnabled(true);
			radDeferred.setEnabled(true);
		} else {
			chkDeferrable.setSelection(false);
			radImmediate.setEnabled(false);
			radDeferred.setEnabled(false);
		}
		
		JiemamyContext context = getContext();
		KeyConstraintModel referenceKeyConstraint =
				DefaultTableModel.findReferencedKeyConstraint(context.getTables(), foreignKey);
		if (referenceKeyConstraint == null) {
			cmbReferenceKey.setText(cmbReferenceKey.getItem(0));
		} else {
			
			cmbReferenceKey.setText(toReferenceKeyLabel(referenceKeyConstraint));
		}
		
		assert dialogArea != null;
		createMappingComponents(dialogArea);
		
		return super.open();
	}
	
	@Override
	protected boolean canExecuteOk() {
		for (Combo combo : keyColumnCombos) {
			if (combo.getSelectionIndex() == -1) {
				return false;
			}
		}
		return super.canExecuteOk();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		logger.debug(LogMarker.LIFECYCLE, "createDialogArea");
		getShell().setText(Messages.Dialog_Title);
		
		dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayout(new GridLayout(5, false));
		
		Label label;
		GridData gd;
		
		label = new Label(dialogArea, SWT.NULL);
		label.setText("制約名(&N):"); // RESOURCE
		
		txtKeyName = new Text(dialogArea, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		txtKeyName.setLayoutData(gd);
		txtKeyName.addFocusListener(new TextSelectionAdapter(txtKeyName));
		
		label = new Label(dialogArea, SWT.NULL);
		label.setText("参照キー(&K):"); // RESOURCE
		
		cmbReferenceKey = new Combo(dialogArea, SWT.READ_ONLY); // TODO CComboにしてPKラベルを表示
		cmbReferenceKey.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (LocalKeyConstraintModel referenceKey : referenceKeys) {
			cmbReferenceKey.add(toReferenceKeyLabel(referenceKey));
		}
		
		cmbReferenceKey.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				logger.debug(LogMarker.LIFECYCLE, "cmbReferenceKey selected");
				createMappingComponents(grpMapping);
			}
			
		});
		
		createDeferrabilityComponents(dialogArea);
		
		final Composite option = new Composite(dialogArea, SWT.NULL);
		option.setLayout(new GridLayout(4, false));
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 5;
		option.setLayoutData(gd);
		
		createReferentialActionComponents(option);
		
		label = new Label(option, SWT.NULL);
		label.setText("マッチ型(&M)"); // RESOURCE
		cmbMatchType = new Combo(option, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		cmbMatchType.setLayoutData(gd);
		cmbMatchType.add("");
		for (MatchType matchType : MatchType.values()) {
			cmbMatchType.add(matchType.name());
		}
		
		return dialogArea;
	}
	
	@Override
	protected Point getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	@Override
	protected boolean performOk() {
		logger.debug(LogMarker.LIFECYCLE, "performOk");
		DefaultForeignKeyConstraintModel foreignKey = getTargetCoreModel();
		
		String name = StringUtils.defaultString(txtKeyName.getText());
		foreignKey.setName(name);
		
		MatchType matchType = null;
		try {
			matchType = MatchType.valueOf(cmbMatchType.getText());
		} catch (IllegalArgumentException e) {
			logger.warn(cmbMatchType.getText() + " is not MatchType element.");
		}
		foreignKey.setMatchType(matchType);
		
		ReferentialAction onDelete = null;
		try {
			onDelete = ReferentialAction.valueOf(cmbOnDelete.getText());
		} catch (IllegalArgumentException e) {
			logger.warn(cmbOnDelete.getText() + " is not ReferentialAction element.");
		}
		foreignKey.setOnDelete(onDelete);
		
		ReferentialAction onUpdate = null;
		try {
			onUpdate = ReferentialAction.valueOf(cmbOnUpdate.getText());
		} catch (IllegalArgumentException e) {
			logger.warn(cmbOnUpdate.getText() + " is not ReferentialAction element.");
		}
		foreignKey.setOnUpdate(onUpdate);
		
		if (chkDeferrable.getSelection()) {
			InitiallyCheckTime initiallyCheckTime = null;
			if (radImmediate.getSelection()) {
				initiallyCheckTime = InitiallyCheckTime.IMMEDIATE;
			} else if (radDeferred.getSelection()) {
				initiallyCheckTime = InitiallyCheckTime.DEFERRED;
			}
			foreignKey.setDeferrability(DefaultDeferrabilityModel.valueOf(true, initiallyCheckTime));
		} else {
			foreignKey.setDeferrability(null);
		}
		
		foreignKey.clearKeyColumns();
		int selectionIndex = cmbReferenceKey.getSelectionIndex();
		LocalKeyConstraintModel referenceKeyConstraint = referenceKeys.get(selectionIndex);
		for (EntityRef<? extends ColumnModel> referenceColumnRef : referenceKeyConstraint.getKeyColumns()) {
			foreignKey.addReferenceColumn(referenceColumnRef);
		}
		
		for (int i = 0; i < keyColumnCombos.size(); i++) {
			int index = keyColumnCombos.get(i).getSelectionIndex();
			ColumnModel keyColumn = sourceColumns.get(index);
			foreignKey.addKeyColumn(keyColumn.toReference());
		}
		
		return true;
	}
	
	private void createDeferrabilityComponents(Composite parent) {
		chkDeferrable = new Button(parent, SWT.CHECK);
		chkDeferrable.setText("遅延可能(&A)"); // RESOURCE
		chkDeferrable.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				if (chkDeferrable.getSelection()) {
					radImmediate.setEnabled(true);
					radDeferred.setEnabled(true);
				} else {
					radImmediate.setEnabled(false);
					radDeferred.setEnabled(false);
				}
			}
		});
		
		radImmediate = new Button(parent, SWT.RADIO);
		radImmediate.setText("即時評価(&I)"); // RESOURCE
		
		radDeferred = new Button(parent, SWT.RADIO);
		radDeferred.setText("遅延評価(&R)"); // RESOURCE
	}
	
	private void createMappingComponents(final Composite parent) {
		keyColumnCombos.clear();
		referenceColumnLabels.clear();
		
		Label label;
		GridData gd;
		
		if (grpMapping == null || grpMapping.isDisposed()) {
			grpMapping = new Group(parent, SWT.NULL);
			grpMapping.setText("マッピング"); // RESOURCE
			grpMapping.setLayout(new GridLayout(3, false));
			gd = new GridData(GridData.FILL_BOTH);
			gd.horizontalSpan = 5;
			grpMapping.setLayoutData(gd);
		}
		
		int selectionIndex = cmbReferenceKey.getSelectionIndex();
		LocalKeyConstraintModel referenceKeyConstraint = referenceKeys.get(selectionIndex);
		
		if (referenceKeyConstraint.getKeyColumns().size() == 0) {
			label = new Label(grpMapping, SWT.NULL);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			label.setLayoutData(gd);
			label.setText("参照するカラムがありません"); // RESOURCE
		} else {
			for (Control control : grpMapping.getChildren()) {
				control.dispose();
			}
			
			// ヘッダ
			label = new Label(grpMapping, SWT.BORDER);
			label.setText("参照元"); // RESOURCE
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			label.setAlignment(SWT.CENTER);
			
			label = new Label(grpMapping, SWT.NULL);
			label.setText("=>"); // $NON-NLS-1$
			
			label = new Label(grpMapping, SWT.BORDER);
			label.setText("参照先"); // RESOURCE
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			label.setAlignment(SWT.CENTER);
			
			JiemamyContext context = getContext();
			
			ForeignKeyConstraintModel foreignKey = getTargetCoreModel();
			for (EntityRef<? extends ColumnModel> referenceColumnRef : referenceKeyConstraint.getKeyColumns()) {
				Combo cmbKeyColumn = new Combo(grpMapping, SWT.READ_ONLY);
				cmbKeyColumn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				for (ColumnModel col : sourceColumns) {
					cmbKeyColumn.add(col.getName());
				}
				
				int index = foreignKey.getReferenceColumns().indexOf(referenceColumnRef);
				if (index != -1) {
					EntityRef<? extends ColumnModel> keyColumnRef = foreignKey.getKeyColumns().get(index);
					ColumnModel keyColumnModel = context.resolve(keyColumnRef);
					cmbKeyColumn.setText(keyColumnModel.getName());
				} else {
					cmbKeyColumn.setText(cmbKeyColumn.getItem(0));
				}
				cmbKeyColumn.setVisibleItemCount(20);
				keyColumnCombos.add(cmbKeyColumn);
				
				label = new Label(grpMapping, SWT.NULL);
				label.setText("=>"); // $NON-NLS-1$
				
				ColumnModel referenceColumnModel = context.resolve(referenceColumnRef);
				Label lblReferenceColumn = new Label(grpMapping, SWT.BORDER);
				lblReferenceColumn.setText(referenceColumnModel.getName());
				lblReferenceColumn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				referenceColumnLabels.add(lblReferenceColumn);
			}
			
			getShell().pack(true);
			parent.layout(false);
		}
	}
	
	private void createReferentialActionComponents(Composite parent) {
		Label label;
		label = new Label(parent, SWT.NULL);
		label.setText("ON DELETE(&D)"); // RESOURCE
		cmbOnDelete = new Combo(parent, SWT.READ_ONLY);
		cmbOnDelete.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmbOnDelete.add(StringUtils.EMPTY);
		for (ReferentialAction referentialAction : ReferentialAction.values()) {
			cmbOnDelete.add(referentialAction.name());
		}
		
		label = new Label(parent, SWT.NULL);
		label.setText("ON UPDATE(&U)"); // RESOURCE
		cmbOnUpdate = new Combo(parent, SWT.READ_ONLY);
		cmbOnUpdate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmbOnUpdate.add(StringUtils.EMPTY);
		for (ReferentialAction referentialAction : ReferentialAction.values()) {
			cmbOnUpdate.add(referentialAction.name());
		}
	}
	
	private String toReferenceKeyLabel(KeyConstraintModel referenceKey) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isEmpty(referenceKey.getName()) == false) {
			sb.append(referenceKey.getName());
			sb.append(" ");
		}
		sb.append(KeyConstraintUtil.toStringKeyColumns(getContext(), referenceKey));
		return sb.toString();
	}
}
