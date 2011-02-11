/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/18
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.JiemamyCorePlugin;
import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractEditListener;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractTab;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractTableEditor;
import org.jiemamy.eclipse.core.ui.editor.diagram.DefaultTableEditorConfig;
import org.jiemamy.eclipse.core.ui.editor.diagram.EditListener;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.eclipse.core.ui.utils.LabelStringUtil;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.eclipse.extension.ExtensionResolver;
import org.jiemamy.model.column.ColumnParameterKey;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.column.SimpleJmColumn;
import org.jiemamy.model.constraint.JmNotNullConstraint;
import org.jiemamy.model.constraint.SimpleJmKeyConstraint;
import org.jiemamy.model.constraint.SimpleJmNotNullConstraint;
import org.jiemamy.model.constraint.SimpleJmPrimaryKeyConstraint;
import org.jiemamy.model.datatype.DataType;
import org.jiemamy.model.datatype.RawTypeDescriptor;
import org.jiemamy.model.datatype.SimpleDataType;
import org.jiemamy.model.datatype.TypeParameterKey;
import org.jiemamy.model.domain.JmDomain;
import org.jiemamy.model.domain.SimpleJmDomain.DomainType;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.table.SimpleJmTable;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.transaction.StoredEventListener;
import org.jiemamy.utils.LogMarker;

/**
 * テーブル編集ダイアログの「カラム」タブ。
 * 
 * @author daisuke
 */
public class TableEditDialogColumnTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditDialogColumnTab.class);
	
	private final JiemamyContext context;
	
	private final SimpleJmTable table;
	
	private final Dialect dialect;
	
	private List<RawTypeDescriptor> allTypes;
	
	private AbstractTableEditor columnTableEditor;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param context コンテキスト
	 * @param table 編集対象{@link JmTable}
	 */
	public TableEditDialogColumnTab(TabFolder parentTabFolder, int style, JiemamyContext context, SimpleJmTable table) {
		super(parentTabFolder, style, Messages.Tab_Table_Columns);
		
		this.context = context;
		this.table = table;
		
		Dialect dialect = null;
		try {
			dialect = context.findDialect();
		} catch (ClassNotFoundException e) {
			dialect = JiemamyCorePlugin.getDialectResolver().getAllInstance().get(0);
			logger.warn("Dialectのロスト", e);
		}
		assert dialect != null;
		this.dialect = dialect;
		
		int size = context.getDomains().size() + dialect.getAllRawTypeDescriptors().size();
		List<RawTypeDescriptor> rawTypes = Lists.newArrayListWithExpectedSize(size);
		rawTypes.addAll(dialect.getAllRawTypeDescriptors());
		rawTypes.addAll(Lists.transform(Lists.newArrayList(context.getDomains()),
				new Function<JmDomain, RawTypeDescriptor>() {
					
					public RawTypeDescriptor apply(JmDomain domain) {
						return domain.asType();
					}
				}));
		allTypes = ImmutableList.copyOf(rawTypes);
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		columnTableEditor = new ColumnTableEditor(composite, SWT.NULL);
		columnTableEditor.configure();
		columnTableEditor.disableEditControls();
		
		getTabItem().setControl(composite);
	}
	
	@Override
	public boolean isTabComplete() {
		return true;
	}
	

	/**
	 * カラム用{@link IContentProvider}実装クラス。
	 * 
	 * @author daisuke
	 */
	private class ColumnContentProvider implements IStructuredContentProvider, StoredEventListener {
		
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "disposed");
		}
		
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof JmTable) {
				return ((JmTable) inputElement).getColumns().toArray();
			}
			logger.error("unknown input: " + inputElement.getClass().getName());
			return new Object[0];
		}
		
		public void handleStoredEvent(StoredEvent<?> event) {
			logger.debug(LogMarker.LIFECYCLE, "commandExecuted");
			columnTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
		}
	}
	
	/**
	 * カラム用{@link ITableLabelProvider}実装クラス。
	 * 
	 * @author daisuke
	 */
	private class ColumnLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			if ((element instanceof JmColumn) == false) {
				logger.error("unknown element: " + element.getClass().getName());
				return null;
			}
			
			JmColumn column = (JmColumn) element;
			ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
			
			switch (columnIndex) {
				case 0:
					return table.isPrimaryKeyColumn(column.toReference()) ? ir.get(Images.ICON_PK) : null;
					
				case 4:
					return ir.get(table.isNotNullColumn(column.toReference()) ? Images.CHECK_ON : Images.CHECK_OFF);
					
				default:
					return null;
			}
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if ((element instanceof JmColumn) == false) {
				logger.error("unknown element: " + element.getClass().getName());
				return StringUtils.EMPTY;
			}
			
			JmColumn column = (JmColumn) element;
			switch (columnIndex) {
				case 1:
					return column.getName();
					
				case 2:
					return LabelStringUtil.toString(dialect, column.getDataType());
					
				case 3:
					return column.getDefaultValue();
					
				default:
					return StringUtils.EMPTY;
			}
		}
	}
	
	private class ColumnTableEditor extends AbstractTableEditor {
		
		private static final int COL_WIDTH_NAME = 200;
		
		private static final int COL_WIDTH_TYPE = 150;
		
		private static final int COL_WIDTH_DEFAULT = 120;
		
		private static final int COL_WIDTH_NN = 40;
		
		private final EditListener editListener = new EditListenerImpl();
		
		private Text txtColumnName;
		
		private Text txtColumnLogicalName;
		
		private Combo cmbDataType;
		
		private Text txtDefaultValue;
		
		private Button chkIsNotNull;
		
		private Button chkIsPK;
		
		private Button chkIsDisabled;
		
		private Text txtDescription;
		
		private Composite cmpTypeOption;
		
		private Map<EntityRef<? extends JmColumn>, TypeParameterManager> typeParameterManagers = Maps.newHashMap();
		
		private TypeParameterHandler typeOptionHandler;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public ColumnTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("カラム情報")); // RESOURCE
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			for (RawTypeDescriptor typeDesc : allTypes) {
				cmbDataType.add(typeDesc.getTypeName());
			}
			
			// 各コントロールに大して、操作した時にモデルにを更新するリスナを仕掛ける
			txtColumnName.addKeyListener(editListener);
			txtColumnLogicalName.addKeyListener(editListener);
			cmbDataType.addSelectionListener(editListener);
			chkIsPK.addSelectionListener(editListener);
			chkIsNotNull.addSelectionListener(editListener);
			chkIsDisabled.addSelectionListener(editListener);
			txtDefaultValue.addKeyListener(editListener);
			txtDescription.addKeyListener(editListener);
			
			// テキスト入力widgetに対して、フォーカスした時に内部のテキストを全選択状態にするリスナを仕掛ける
			txtColumnName.addFocusListener(new TextSelectionAdapter(txtColumnName));
			txtColumnLogicalName.addFocusListener(new TextSelectionAdapter(txtColumnLogicalName));
			txtDefaultValue.addFocusListener(new TextSelectionAdapter(txtDefaultValue));
			txtDescription.addFocusListener(new TextSelectionAdapter(txtDefaultValue));
			
			// データ型選択コンボに対して、操作した時に、その型に必要なオプションwidgetを制御するリスナを仕掛ける
			cmbDataType.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					Table swtTable = getTableViewer().getTable();
					int index = swtTable.getSelectionIndex();
					if (index < 0 || index >= swtTable.getItemCount()) {
						return;
					}
					
					SimpleJmColumn column = (SimpleJmColumn) getTableViewer().getElementAt(index);
					TypeParameterManager typeOptionManager = typeParameterManagers.get(column.toReference());
					RawTypeDescriptor typeDesc = allTypes.get(cmbDataType.getSelectionIndex());
					Set<TypeParameterKey<?>> keys = dialect.getTypeParameterSpecs(typeDesc).keySet();
					typeOptionManager.createTypeParameterControls(column, keys);
				}
			});
		}
		
		@Override
		protected void configureTableViewer(TableViewer tableViewer) {
			tableViewer.setLabelProvider(new ColumnLabelProvider());
			final ColumnContentProvider contentProvider = new ColumnContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(table);
			
			final EventBroker eventBroker = table.getEventBroker();
			eventBroker.addListener(contentProvider);
			
			// THINK んーーー？？ このタイミングか？ AbstractTableEditor#dispose かな？
			tableViewer.getTable().addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					eventBroker.removeListener(contentProvider);
				}
				
			});
			
			ExtensionResolver<Dialect> dialectResolver = JiemamyCorePlugin.getDialectResolver();
			IConfigurationElement dialectElement =
					dialectResolver.getExtensionConfigurationElements()
						.get(context.getMetadata().getDialectClassName());
			if (dialectElement != null) {
				IConfigurationElement[] children = dialectElement.getChildren("typeOptionHandler");
				if (ArrayUtils.isEmpty(children) == false) {
					try {
						typeOptionHandler = (TypeParameterHandler) children[0].createExecutableExtension("class");
					} catch (Exception e) {
						ExceptionHandler.handleException(e);
					}
				}
			}
			
			typeParameterManagers.clear();
			for (JmColumn column : table.getColumns()) {
				TypeParameterManager typeOptionManager =
						new TypeParameterManager(dialect, cmpTypeOption, editListener, typeOptionHandler);
				typeParameterManagers.put(column.toReference(), typeOptionManager);
			}
		}
		
		@Override
		protected void createEditorControls(Composite parent) {
			GridLayout layout;
			GridData gd;
			Label label;
			
			Composite cmpBasic = new Composite(parent, SWT.NULL);
			cmpBasic.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			layout = new GridLayout(4, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpBasic.setLayout(layout);
			
			label = new Label(cmpBasic, SWT.NULL);
			label.setText("カラム名(&M)"); // RESOURCE
			
			txtColumnName = new Text(cmpBasic, SWT.BORDER);
			txtColumnName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpBasic, SWT.NULL);
			label.setText("論理名(&L)"); // RESOURCE
			
			txtColumnLogicalName = new Text(cmpBasic, SWT.BORDER);
			txtColumnLogicalName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpBasic, SWT.NULL);
			label.setText("データ型(&T)"); // RESOURCE
			
			Composite cmpTypes = new Composite(cmpBasic, SWT.NULL);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			cmpTypes.setLayoutData(gd);
			layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpTypes.setLayout(layout);
			
			cmbDataType = new Combo(cmpTypes, SWT.READ_ONLY);
			cmbDataType.setVisibleItemCount(20);
			
			cmpTypeOption = new Composite(cmpTypes, SWT.NULL);
			cmpTypeOption.setLayout(new RowLayout());
			gd = new GridData();
			gd.heightHint = 25; // CHECKSTYLE IGNORE THIS LINE
			gd.widthHint = 400; // CHECKSTYLE IGNORE THIS LINE
			cmpTypeOption.setLayoutData(gd);
			
			Composite cmpChecks = new Composite(parent, SWT.NULL);
			RowLayout rowLayout = new RowLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpChecks.setLayout(rowLayout);
			
			chkIsPK = new Button(cmpChecks, SWT.CHECK);
			chkIsPK.setText("主キー(&P)"); // RESOURCE
			
			chkIsNotNull = new Button(cmpChecks, SWT.CHECK);
			chkIsNotNull.setText("NOT NULL(&O)"); // RESOURCE
			
			chkIsDisabled = new Button(cmpChecks, SWT.CHECK);
			chkIsDisabled.setText("無効(&G)"); // RESOURCE
			
			createAdvancedEditComponents(parent);
		}
		
		@Override
		protected void createTableColumns(Table swtTable) {
			TableColumn colMark = new TableColumn(swtTable, SWT.LEFT);
			colMark.setText(StringUtils.EMPTY);
			colMark.setWidth(20);
			
			TableColumn colName = new TableColumn(swtTable, SWT.LEFT);
			colName.setText("カラム名"); // RESOURCE
			colName.setWidth(COL_WIDTH_NAME);
			
			TableColumn colType = new TableColumn(swtTable, SWT.LEFT);
			colType.setText("データ型"); // RESOURCE
			colType.setWidth(COL_WIDTH_TYPE);
			
			TableColumn colSimple = new TableColumn(swtTable, SWT.LEFT);
			colSimple.setText("デフォルト値"); // RESOURCE
			colSimple.setWidth(COL_WIDTH_DEFAULT);
			
			TableColumn colNotNull = new TableColumn(swtTable, SWT.LEFT);
			colNotNull.setText("NN");
			colNotNull.setWidth(COL_WIDTH_NN);
		}
		
		@Override
		protected void disableEditorControls() {
			txtColumnName.setText(StringUtils.EMPTY);
			txtColumnLogicalName.setText(StringUtils.EMPTY);
			cmbDataType.setText(StringUtils.EMPTY);
			chkIsPK.setSelection(false);
			chkIsNotNull.setSelection(false);
			chkIsDisabled.setSelection(false);
			txtDefaultValue.setText(StringUtils.EMPTY);
			txtDescription.setText(StringUtils.EMPTY);
			
			txtColumnName.setEnabled(false);
			txtColumnLogicalName.setEnabled(false);
			cmbDataType.setEnabled(false);
			chkIsPK.setEnabled(false);
			chkIsNotNull.setEnabled(false);
			chkIsDisabled.setEnabled(false);
			txtDefaultValue.setEnabled(false);
			txtDescription.setEnabled(false);
			
			for (Control control : cmpTypeOption.getChildren()) {
				control.dispose();
			}
		}
		
		@Override
		protected void enableEditorControls(int index) {
			SimpleJmColumn column = (SimpleJmColumn) getTableViewer().getElementAt(index);
			
			txtColumnName.setEnabled(true);
			txtColumnLogicalName.setEnabled(true);
			cmbDataType.setEnabled(true);
			txtDefaultValue.setEnabled(true);
			txtDescription.setEnabled(true);
			chkIsPK.setEnabled(true);
			chkIsNotNull.setEnabled(true);
			chkIsDisabled.setEnabled(true);
			
			DataType dataType = column.getDataType();
			Set<TypeParameterKey<?>> keys = dialect.getTypeParameterSpecs(dataType.getRawTypeDescriptor()).keySet();
			TypeParameterManager manager = typeParameterManagers.get(column.toReference());
			manager.createTypeParameterControls(column, keys);
			
			// 現在値の設定
			txtColumnName.setText(column.getName());
			txtColumnLogicalName.setText(StringUtils.defaultString(column.getLogicalName()));
			chkIsNotNull.setSelection(table.isNotNullColumn(column.toReference()));
			
			if (dataType.getRawTypeDescriptor() instanceof DomainType) {
				DomainType domainType = (DomainType) dataType.getRawTypeDescriptor();
				JmDomain domain = context.resolve(domainType);
				cmbDataType.setText(domain.getName());
			} else {
				cmbDataType.setText(dataType.getRawTypeDescriptor().getTypeName());
				typeParameterManagers.get(column.toReference()).setParametersToControl(column.getDataType());
			}
			txtDefaultValue.setText(StringUtils.defaultString(column.getDefaultValue()));
			txtDescription.setText(StringUtils.defaultString(column.getDescription()));
			
			chkIsPK.setSelection(table.isPrimaryKeyColumn(column.toReference()));
			
			Boolean disabled = column.getParam(ColumnParameterKey.DISABLED);
			if (disabled != null && disabled) {
				chkIsDisabled.setSelection(true);
			} else {
				chkIsDisabled.setSelection(false);
			}
		}
		
		@Override
		protected void performAddItem() {
			Table swtTable = getTableViewer().getTable();
			SimpleJmColumn column = new SimpleJmColumn();
			
			String newName = "COLUMN_" + (table.getColumns().size() + 1);
			column.setName(newName); // TODO autoname
			
			SimpleDataType type = new SimpleDataType(allTypes.get(0));
			column.setDataType(type);
			table.store(column);
			
			TypeParameterManager typeOptionManager =
					new TypeParameterManager(dialect, cmpTypeOption, editListener, typeOptionHandler);
			typeParameterManagers.put(column.toReference(), typeOptionManager);
			
			int addedIndex = column.getIndex();
			swtTable.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtColumnName.setFocus();
		}
		
		@Override
		protected void performInsertItem() {
			Table swtTable = getTableViewer().getTable();
			int index = swtTable.getSelectionIndex();
			
			SimpleJmColumn column = new SimpleJmColumn();
			
			String newName = "COLUMN_" + (table.getColumns().size() + 1);
			column.setName(newName); // TODO autoname
			
			SimpleDataType type = new SimpleDataType(allTypes.get(0));
			column.setDataType(type);
			column.setIndex(index);
			table.store(column);
			
			TypeParameterManager typeOptionManager =
					new TypeParameterManager(dialect, cmpTypeOption, editListener, typeOptionHandler);
			typeParameterManagers.put(column.toReference(), typeOptionManager);
			
			int addedIndex = table.getColumns().indexOf(column);
			swtTable.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtColumnName.setFocus();
		}
		
		@Override
		protected void performMoveDownItem() {
			Table swtTable = getTableViewer().getTable();
			int index = swtTable.getSelectionIndex();
			if (index < 0 || index >= swtTable.getItemCount()) {
				return;
			}
			
			table.swapColumn(index, index + 1);
			
			swtTable.setSelection(index + 1);
			enableEditControls(index + 1);
		}
		
		@Override
		protected void performMoveUpItem() {
			Table swtTable = getTableViewer().getTable();
			int index = swtTable.getSelectionIndex();
			if (index <= 0 || index > swtTable.getItemCount()) {
				return;
			}
			
			table.swapColumn(index - 1, index);
			
			swtTable.setSelection(index - 1);
			enableEditControls(index - 1);
		}
		
		@Override
		protected void performRemoveItem() {
			TableViewer tableViewer = getTableViewer();
			Table swtTable = tableViewer.getTable();
			int index = swtTable.getSelectionIndex();
			if (index < 0 || index > swtTable.getItemCount()) {
				return;
			}
			
			JmColumn subject = (JmColumn) getTableViewer().getElementAt(index);
			
			// 削除対象カラムに対するNNは削除
			Set<JmNotNullConstraint> nns = table.getConstraints(JmNotNullConstraint.class);
			for (JmNotNullConstraint nn : nns) {
				if (nn.getColumn().isReferenceOf(subject)) {
					table.deleteConstraint(nn.toReference());
				}
			}
			
			// 削除対象カラムがキーの一部になっていたら、そのキーセットからカラムを削除
			Set<SimpleJmKeyConstraint> keys = table.getConstraints(SimpleJmKeyConstraint.class);
			for (SimpleJmKeyConstraint key : keys) {
				if (key.getKeyColumns().contains(subject.toReference())) {
					key.removeKeyColumn(subject.toReference());
					table.store(key);
				}
			}
			
			table.deleteColumn(subject.toReference());
			
			tableViewer.remove(subject);
			int nextSelection = swtTable.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				swtTable.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			swtTable.setFocus();
			
			typeParameterManagers.remove(subject.toReference());
		}
		
		/**
		 * 「高度な設定」のUIを構築する。
		 * 
		 * @param parent 親コンポーネント
		 */
		private void createAdvancedEditComponents(Composite parent) {
			GridLayout layout;
			Label label;
			Group cmpAdvanced = new Group(parent, SWT.NULL);
			cmpAdvanced.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			layout = new GridLayout(4, false);
			cmpAdvanced.setLayout(layout);
			cmpAdvanced.setText("高度な設定"); // RESOURCE
			
			label = new Label(cmpAdvanced, SWT.NULL);
			label.setText("デフォルト値(&F)"); // RESOURCE
			
			txtDefaultValue = new Text(cmpAdvanced, SWT.BORDER);
			txtDefaultValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpAdvanced, SWT.NULL);
			label.setText("説明(&D)"); // RESOURCE
			
			txtDescription = new Text(cmpAdvanced, SWT.BORDER | SWT.BORDER);
			txtDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		private void updateModel() {
			int editIndex = getTableViewer().getTable().getSelectionIndex();
			if (editIndex == -1) {
				return;
			}
			
			SimpleJmColumn column = (SimpleJmColumn) table.getColumns().get(editIndex);
			
			String columnName = StringUtils.defaultString(txtColumnName.getText());
			column.setName(columnName);
			
			String logicalName = StringUtils.defaultString(txtColumnLogicalName.getText());
			column.setLogicalName(logicalName);
			
			int selectionInedx = cmbDataType.getSelectionIndex();
			if (selectionInedx != -1) {
				SimpleDataType dataType = new SimpleDataType(allTypes.get(selectionInedx));
				column.setDataType(dataType);
			}
			
			String defaultValue = StringUtils.defaultString(txtDefaultValue.getText());
			column.setDefaultValue(defaultValue);
			
			String description = StringUtils.defaultString(txtDescription.getText());
			column.setDescription(description);
			
			if (chkIsNotNull.getSelection() == false) {
				JmNotNullConstraint nn = table.getNotNullConstraintFor(column.toReference());
				if (nn != null) {
					table.deleteConstraint(nn.toReference());
				}
			} else if (table.getNotNullConstraintFor(column.toReference()) == null) {
				table.store(SimpleJmNotNullConstraint.of(column));
			}
			
			SimpleJmPrimaryKeyConstraint primaryKey = (SimpleJmPrimaryKeyConstraint) table.getPrimaryKey();
			if (chkIsPK.getSelection() == false) {
				if (primaryKey != null) {
					primaryKey.removeKeyColumn(column.toReference());
					table.store(primaryKey);
				}
			} else {
				if (primaryKey == null) {
					primaryKey = SimpleJmPrimaryKeyConstraint.of(column);
					table.store(primaryKey);
				} else if (primaryKey.getKeyColumns().contains(column.toReference()) == false) {
					primaryKey.addKeyColumn(column.toReference());
					table.store(primaryKey);
				}
			}
			
			// THINK
//			if (primaryKey != null && primaryKey.getKeyColumns().size() <= 0) {
//				table.deleteConstraint(primaryKey.toReference());
//			}
			
			Boolean disabled = column.getParam(ColumnParameterKey.DISABLED);
			if (chkIsDisabled.getSelection() == false) {
				if (disabled != null && disabled) {
					column.removeParam(ColumnParameterKey.DISABLED);
				}
			} else {
				if (disabled == null || disabled == false) {
					column.putParam(ColumnParameterKey.DISABLED, true);
				}
			}
			
			TypeParameterManager manager = typeParameterManagers.get(column.toReference());
			manager.setParametersFromControl(column);
			
			table.store(column);
		}
		

		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
			}
		}
	}
}
