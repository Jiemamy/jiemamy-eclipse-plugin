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
package org.jiemamy.eclipse.core.ui.editor.dialog.table;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Collections2;
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
import org.jiemamy.dddbase.Entity;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.dialect.TypeParameterSpec;
import org.jiemamy.eclipse.JiemamyCorePlugin;
import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.DisplayPlace;
import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractEditListener;
import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractTab;
import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractTableEditor;
import org.jiemamy.eclipse.core.ui.editor.dialog.DefaultTableEditorConfig;
import org.jiemamy.eclipse.core.ui.editor.dialog.EditListener;
import org.jiemamy.eclipse.core.ui.editor.dialog.TextSelectionAdapter;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.eclipse.core.ui.utils.LabelStringUtil;
import org.jiemamy.eclipse.core.ui.utils.SpecsToKeys;
import org.jiemamy.eclipse.extension.ExtensionResolver;
import org.jiemamy.model.DatabaseObjectModel;
import org.jiemamy.model.column.ColumnModel;
import org.jiemamy.model.column.DefaultColumnModel;
import org.jiemamy.model.constraint.DefaultNotNullConstraintModel;
import org.jiemamy.model.constraint.DefaultPrimaryKeyConstraintModel;
import org.jiemamy.model.constraint.NotNullConstraintModel;
import org.jiemamy.model.datatype.DefaultTypeVariant;
import org.jiemamy.model.datatype.TypeParameterKey;
import org.jiemamy.model.datatype.TypeReference;
import org.jiemamy.model.datatype.TypeVariant;
import org.jiemamy.model.domain.DefaultDomainModel.DomainType;
import org.jiemamy.model.domain.DomainModel;
import org.jiemamy.model.table.DefaultTableModel;
import org.jiemamy.model.table.TableModel;
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
	
	private final DefaultTableModel tableModel;
	
	private final Dialect dialect;
	
	private List<TypeReference> allTypes;
	
	private AbstractTableEditor columnTableEditor;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param context コンテキスト
	 * @param tableModel 編集対象{@link TableModel}
	 */
	public TableEditDialogColumnTab(TabFolder parentTabFolder, int style, JiemamyContext context,
			DefaultTableModel tableModel) {
		super(parentTabFolder, style, Messages.Tab_Table_Columns);
		
		this.context = context;
		this.tableModel = tableModel;
		
		Dialect dialect = null;
		try {
			dialect = context.findDialect();
		} catch (ClassNotFoundException e) {
			dialect = JiemamyCorePlugin.getDialectResolver().getAllInstance().get(0);
			logger.warn("Dialectのロスト", e);
		}
		assert dialect != null;
		this.dialect = dialect;
		
		int size = context.getDomains().size() + dialect.getAllTypeReferences().size();
		allTypes = Lists.newArrayListWithExpectedSize(size);
		
		allTypes.addAll(dialect.getAllTypeReferences());
		for (DomainModel domainModel : context.getDomains()) {
			allTypes.add(domainModel.asType());
		}
		
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
	private class ColumnContentProvider implements IStructuredContentProvider, StoredEventListener<DatabaseObjectModel> {
		
		public void commandExecuted(StoredEvent<DatabaseObjectModel> command) {
			logger.debug(LogMarker.LIFECYCLE, "commandExecuted");
			columnTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "disposed");
		}
		
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof TableModel) {
				return ((TableModel) inputElement).getColumns().toArray();
			}
			logger.error("unknown input: " + inputElement.getClass().getName());
			return new Object[0];
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
			if ((element instanceof ColumnModel) == false) {
				logger.error("unknown element: " + element.getClass().getName());
				return null;
			}
			
			ColumnModel columnModel = (ColumnModel) element;
			ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
			
			switch (columnIndex) {
				case 0:
					return tableModel.isPrimaryKeyColumn(columnModel.toReference()) ? ir.get(Images.ICON_PK) : null;
					
				case 4:
					return ir.get(tableModel.isNotNullColumn(columnModel.toReference()) ? Images.CHECK_ON
							: Images.CHECK_OFF);
					
				default:
					return null;
			}
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if ((element instanceof ColumnModel) == false) {
				logger.error("unknown element: " + element.getClass().getName());
				return StringUtils.EMPTY;
			}
			
			ColumnModel columnModel = (ColumnModel) element;
			switch (columnIndex) {
				case 1:
					return LabelStringUtil.toString(context, columnModel, DisplayPlace.TABLE);
					
				case 2:
					return LabelStringUtil.toString(dialect, columnModel.getDataType(), DisplayPlace.TABLE);
					
				case 3:
					return columnModel.getDefaultValue();
					
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
		
		private Map<EntityRef<? extends ColumnModel>, TypeParameterManager> typeOptionManagers = Maps.newHashMap();
		
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
			
			for (TypeReference reference : allTypes) {
				cmbDataType.add(reference.getTypeName());
			}
			
			txtColumnName.addFocusListener(new TextSelectionAdapter(txtColumnName));
			txtColumnName.addKeyListener(editListener);
			
			txtColumnLogicalName.addFocusListener(new TextSelectionAdapter(txtColumnLogicalName));
			txtColumnLogicalName.addKeyListener(editListener);
			
			cmbDataType.addSelectionListener(editListener);
			cmbDataType.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					Table table = getTableViewer().getTable();
					int index = table.getSelectionIndex();
					if (index < 0 || index >= table.getItemCount()) {
						return;
					}
					
					DefaultColumnModel columnModel = (DefaultColumnModel) getTableViewer().getElementAt(index);
					TypeParameterManager typeOptionManager = typeOptionManagers.get(columnModel.toReference());
					TypeReference dataTypeMold = allTypes.get(cmbDataType.getSelectionIndex());
					Collection<TypeParameterSpec> specs = dialect.getTypeParameterSpecs(dataTypeMold);
					Collection<TypeParameterKey<?>> keys = Collections2.transform(specs, SpecsToKeys.INSTANCE);
					typeOptionManager.createTypeOptionControl(columnModel, keys);
				}
			});
			
			chkIsPK.addSelectionListener(editListener);
			
			chkIsNotNull.addSelectionListener(editListener);
			
			chkIsDisabled.addSelectionListener(editListener);
			
			txtDefaultValue.addFocusListener(new TextSelectionAdapter(txtDefaultValue));
			txtDefaultValue.addKeyListener(editListener);
			
			txtDescription.addFocusListener(new TextSelectionAdapter(txtDefaultValue));
			txtDescription.addKeyListener(editListener);
		}
		
		@Override
		protected void configureTableViewer(TableViewer tableViewer) {
			tableViewer.setLabelProvider(new ColumnLabelProvider());
			final ColumnContentProvider contentProvider = new ColumnContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(tableModel);
			
			final EventBroker eventBroker = tableModel.getEventBroker();
			eventBroker.addListener(contentProvider);
			
			// THINK んーーー？？ このタイミングか？ AbstractTableEditor#dispose かな？
			tableViewer.getTable().addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					eventBroker.removeListener(contentProvider);
				}
				
			});
			
			ExtensionResolver<Dialect> dialectResolver = JiemamyCorePlugin.getDialectResolver();
			IConfigurationElement dialectElement =
					dialectResolver.getExtensionConfigurationElements().get(context.getDialectClassName());
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
			
			typeOptionManagers.clear();
			for (ColumnModel columnModel : tableModel.getColumns()) {
				TypeParameterManager typeOptionManager =
						new TypeParameterManager(dialect, cmpTypeOption, editListener, typeOptionHandler);
				typeOptionManagers.put(columnModel.toReference(), typeOptionManager);
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
		protected void createTableColumns(Table table) {
			TableColumn colMark = new TableColumn(table, SWT.LEFT);
			colMark.setText(StringUtils.EMPTY);
			colMark.setWidth(20);
			
			TableColumn colName = new TableColumn(table, SWT.LEFT);
			colName.setText("カラム名"); // RESOURCE
			colName.setWidth(COL_WIDTH_NAME);
			
			TableColumn colType = new TableColumn(table, SWT.LEFT);
			colType.setText("データ型"); // RESOURCE
			colType.setWidth(COL_WIDTH_TYPE);
			
			TableColumn colDefault = new TableColumn(table, SWT.LEFT);
			colDefault.setText("デフォルト値"); // RESOURCE
			colDefault.setWidth(COL_WIDTH_DEFAULT);
			
			TableColumn colNotNull = new TableColumn(table, SWT.LEFT);
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
			DefaultColumnModel columnModel = (DefaultColumnModel) getTableViewer().getElementAt(index);
			
			txtColumnName.setEnabled(true);
			txtColumnLogicalName.setEnabled(true);
			cmbDataType.setEnabled(true);
			txtDefaultValue.setEnabled(true);
			txtDescription.setEnabled(true);
			chkIsPK.setEnabled(true);
			chkIsNotNull.setEnabled(true);
			chkIsDisabled.setEnabled(true);
			
			TypeVariant dataType = columnModel.getDataType();
			Collection<TypeParameterSpec> specs = dialect.getTypeParameterSpecs(dataType.getTypeReference());
			Collection<TypeParameterKey<?>> keys = Collections2.transform(specs, SpecsToKeys.INSTANCE);
			TypeParameterManager manager = typeOptionManagers.get(columnModel.toReference());
			manager.createTypeOptionControl(columnModel, keys);
			
			// 現在値の設定
			txtColumnName.setText(columnModel.getName());
			txtColumnLogicalName.setText(StringUtils.defaultString(columnModel.getLogicalName()));
			
			chkIsNotNull.setSelection(tableModel.isNotNullColumn(columnModel.toReference()));
			
			if (dataType.getTypeReference() instanceof DomainType) {
				DomainType domainRef = (DomainType) dataType.getTypeReference();
				DomainModel domainModel = context.resolve(domainRef);
				cmbDataType.setText(domainModel.getName());
			} else {
				cmbDataType.setText(dataType.getTypeReference().getTypeName());
				typeOptionManagers.get(columnModel.toReference()).setValue(columnModel);
			}
			txtDefaultValue.setText(StringUtils.defaultString(columnModel.getDefaultValue()));
			txtDescription.setText(StringUtils.defaultString(columnModel.getDescription()));
			
			chkIsPK.setSelection(tableModel.isPrimaryKeyColumn(columnModel.toReference()));
			
//			if (columnModel.hasAdapter(Disablable.class)
//					&& Boolean.TRUE.equals(columnModel.getAdapter(Disablable.class).isDisabled())) {
//				chkIsDisabled.setSelection(true);
//			} else {
//				chkIsDisabled.setSelection(false);
//			}
		}
		
		@Override
		protected ColumnModel performAddItem() {
			Table table = getTableViewer().getTable();
			DefaultColumnModel columnModel = new DefaultColumnModel(UUID.randomUUID());
			
			String newName = "COLUMN_" + (tableModel.getColumns().size() + 1);
			columnModel.setName(newName); // TODO autoname
			
			DefaultTypeVariant type = new DefaultTypeVariant(allTypes.get(0));
			columnModel.setDataType(type);
			tableModel.store(columnModel);
			
			TypeParameterManager typeOptionManager =
					new TypeParameterManager(dialect, cmpTypeOption, editListener, typeOptionHandler);
			typeOptionManagers.put(columnModel.toReference(), typeOptionManager);
			
			int addedIndex = columnModel.getIndex();
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtColumnName.setFocus();
			
			return columnModel;
		}
		
		@Override
		protected ColumnModel performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			
			DefaultColumnModel columnModel = new DefaultColumnModel(UUID.randomUUID());
			
			String newName = "COLUMN_" + (tableModel.getColumns().size() + 1);
			columnModel.setName(newName); // TODO autoname
			
			DefaultTypeVariant type = new DefaultTypeVariant(allTypes.get(0));
			columnModel.setDataType(type);
			columnModel.setIndex(index);
			tableModel.store(columnModel);
			
			TypeParameterManager typeOptionManager =
					new TypeParameterManager(dialect, cmpTypeOption, editListener, typeOptionHandler);
			typeOptionManagers.put(columnModel.toReference(), typeOptionManager);
			
			int addedIndex = tableModel.getColumns().indexOf(columnModel);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtColumnName.setFocus();
			
			return columnModel;
		}
		
		@Override
		protected void performMoveDownItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index >= table.getItemCount()) {
				return;
			}
			
			ColumnModel object = (ColumnModel) getTableViewer().getElementAt(index + 1);
			object.setIndex(index);
			tableModel.store(object);
			
			table.setSelection(index + 1);
			enableEditControls(index + 1);
		}
		
		@Override
		protected void performMoveUpItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index <= 0 || index > table.getItemCount()) {
				return;
			}
			
			ColumnModel subject = (ColumnModel) getTableViewer().getElementAt(index);
			subject.setIndex(index - 1);
			tableModel.store(subject);
			
			table.setSelection(index - 1);
			enableEditControls(index - 1);
		}
		
		@Override
		protected Entity performRemoveItem() {
			TableViewer tableViewer = getTableViewer();
			Table table = tableViewer.getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index > table.getItemCount()) {
				return null;
			}
			
			ColumnModel subject = (ColumnModel) getTableViewer().getElementAt(index);
			tableModel.delete(subject.toReference());
			
			tableViewer.remove(subject);
			int nextSelection = table.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				table.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			table.setFocus();
			
			typeOptionManagers.remove(subject);
			
			return subject;
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
			
			DefaultColumnModel columnModel = (DefaultColumnModel) tableModel.getColumns().get(editIndex);
			
			String columnName = StringUtils.defaultString(txtColumnName.getText());
			columnModel.setName(columnName);
			
			String logicalName = StringUtils.defaultString(txtColumnLogicalName.getText());
			columnModel.setLogicalName(logicalName);
			
			int selectionInedx = cmbDataType.getSelectionIndex();
			if (selectionInedx != -1) {
				DefaultTypeVariant dataType = new DefaultTypeVariant(allTypes.get(selectionInedx));
				columnModel.setDataType(dataType);
			}
			
			String defaultValue = StringUtils.defaultString(txtDefaultValue.getText());
			columnModel.setDefaultValue(defaultValue);
			
			String description = StringUtils.defaultString(txtDescription.getText());
			columnModel.setDescription(description);
			
			if (chkIsNotNull.getSelection() == false) {
				NotNullConstraintModel nn = tableModel.getNotNullConstraintFor(columnModel.toReference());
				if (nn != null) {
					tableModel.deleteConstraint(nn.toReference());
				}
			} else if (tableModel.getNotNullConstraintFor(columnModel.toReference()) == null) {
				tableModel.store(DefaultNotNullConstraintModel.of(columnModel));
			}
			
			DefaultPrimaryKeyConstraintModel primaryKey = (DefaultPrimaryKeyConstraintModel) tableModel.getPrimaryKey();
			if (chkIsPK.getSelection() == false) {
				if (primaryKey != null) {
					primaryKey.removeKeyColumn(columnModel.toReference());
					tableModel.store(primaryKey);
				}
			} else {
				if (primaryKey == null) {
					primaryKey = DefaultPrimaryKeyConstraintModel.of(columnModel);
				} else {
					primaryKey.addKeyColumn(columnModel.toReference());
				}
				tableModel.store(primaryKey);
			}
			if (primaryKey != null && primaryKey.getKeyColumns().size() <= 0) {
				tableModel.deleteConstraint(primaryKey.toReference());
			}
			
//			if (chkIsDisabled.getSelection() == false) {
//				if (columnModel.hasAdapter(Disablable.class)) {
//					columnModel.unregisterAdapter(Disablable.class);
//				}
//			} else {
//				if (columnModel.hasAdapter(Disablable.class) == false) {
//					columnModel.registerAdapter(factory.newAdapter(Disablable.class));
//				}
//				columnModel.getAdapter(Disablable.class).setDisabled(true);
//			}
			
			TypeParameterManager manager = typeOptionManagers.get(columnModel.toReference());
			manager.writeBackToAdapter(columnModel);
			
			tableModel.store(columnModel);
		}
		

		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
			}
		}
	}
}
