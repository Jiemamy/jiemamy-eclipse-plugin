/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.editor.dialog.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
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
import org.jiemamy.JiemamyEntity;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.Images;
import org.jiemamy.eclipse.JiemamyCorePlugin;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.editor.DisplayPlace;
import org.jiemamy.eclipse.editor.dialog.AbstractEditListener;
import org.jiemamy.eclipse.editor.dialog.EditListener;
import org.jiemamy.eclipse.editor.dialog.TypeOptionHandler;
import org.jiemamy.eclipse.editor.dialog.TypeOptionManager;
import org.jiemamy.eclipse.editor.utils.LabelStringUtil;
import org.jiemamy.eclipse.extension.ExtensionResolver;
import org.jiemamy.eclipse.ui.AbstractTableEditor;
import org.jiemamy.eclipse.ui.DefaultTableEditorConfig;
import org.jiemamy.eclipse.ui.helper.TextSelectionAdapter;
import org.jiemamy.eclipse.ui.tab.AbstractTab;
import org.jiemamy.eclipse.utils.ExceptionHandler;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.attribute.constraint.PrimaryKeyConstraintModel;
import org.jiemamy.model.dbo.DomainModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.transaction.Command;
import org.jiemamy.transaction.CommandListener;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.utils.LogMarker;
import org.jiemamy.utils.collection.CollectionsUtil;

/**
 * テーブル編集ダイアログの「カラム」タブ。
 * 
 * @author daisuke
 */
public class TableEditDialogColumnTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditDialogColumnTab.class);
	
	private final JiemamyContext rootModel;
	
	private final TableModel tableModel;
	
	private List<DataTypeMold<?>> allTypes;
	
	private AbstractTableEditor columnTableEditor;
	
	private final JiemamyFacade jiemamyFacade;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param tableModel 編集対象{@link TableModel}
	 * @param jiemamyFacade モデル操作を行うファサード
	 */
	public TableEditDialogColumnTab(TabFolder parentTabFolder, int style, TableModel tableModel,
			JiemamyFacade jiemamyFacade) {
		super(parentTabFolder, style, Messages.Tab_Table_Columns);
		
		this.tableModel = tableModel;
		this.jiemamyFacade = jiemamyFacade;
		rootModel = tableModel.getJiemamy().getFactory().getJiemamyContext();
		
		Dialect dialect;
		try {
			dialect = rootModel.findDialect();
		} catch (ClassNotFoundException e) {
			dialect = JiemamyCorePlugin.getDialectResolver().getAllInstance().get(0);
			logger.warn("Dialectのロスト", e);
		}
		
		allTypes = CollectionsUtil.newArrayList(rootModel.getDomains().size() + dialect.getAllDataTypes().size());
		
		allTypes.addAll(dialect.getAllDataTypes());
		allTypes.addAll(rootModel.getDomains());
		
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
		// TODO Auto-generated method stub
		return true;
	}
	

	/**
	 * カラム用ContentProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class ColumnContentProvider extends ArrayContentProvider implements CommandListener {
		
		private Viewer viewer;
		

		public void commandExecuted(Command command) {
			logger.debug(LogMarker.LIFECYCLE, "ColumnContentProvider: commandExecuted");
			columnTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		@Override
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "ColumnContentProvider: disposed");
			super.dispose();
		}
		
		public JiemamyEntity getTargetModel() {
			return (JiemamyEntity) viewer.getInput();
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "ColumnContentProvider: input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
			
			this.viewer = viewer;
			
			super.inputChanged(viewer, oldInput, newInput);
		}
		
	}
	
	/**
	 * カラム用LabelProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class ColumnLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			if ((element instanceof ColumnModel) == false) {
				return null;
			}
			
			ColumnModel columnModel = (ColumnModel) element;
			ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
			
			switch (columnIndex) {
				case 0:
					return columnModel.checkPrimaryKeyColumn() ? ir.get(Images.ICON_PK) : null;
					
				case 4:
					return ir.get(columnModel.getNotNullConstraint() != null ? Images.CHECK_ON : Images.CHECK_OFF);
					
				case 5:
					ColumnCheckConstraint check = columnModel.getCheckConstraint();
					return ir.get(check != null && StringUtils.isEmpty(check.getExpression()) == false
							? Images.CHECK_ON : Images.CHECK_OFF);
					
				default:
					return null;
			}
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if ((element instanceof ColumnModel) == false) {
				return StringUtils.EMPTY;
			}
			
			ColumnModel columnModel = (ColumnModel) element;
			switch (columnIndex) {
				case 1:
					return LabelStringUtil.getString(rootModel, columnModel, DisplayPlace.TABLE);
					
				case 2:
					return LabelStringUtil.getString(rootModel, columnModel.getDataType(), DisplayPlace.TABLE);
					
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
		
		private static final int COL_WIDTH_CHECK = 40;
		
		private final EditListener editListener = new EditListenerImpl();
		
		private final JiemamyContext jiemamy;
		
		private Dialect dialect;
		
		private Text txtColumnName;
		
		private Text txtColumnLogicalName;
		
		private Combo cmbDataType;
		
		private Text txtCheckName;
		
		private Text txtCheckExpression;
		
		private Text txtDefaultValue;
		
		private Button chkIsNotNull;
		
		private Button chkIsPK;
		
		private Button chkIsDisabled;
		
//		private Button chkIsRepresentation;
		
		private Text txtDescription;
		
		private Composite cmpTypeOption;
		
		private Map<ColumnModel, TypeOptionManager> typeOptionManagers = CollectionsUtil.newHashMap();
		
		private final List<? extends ColumnModel> columns;
		
		private TypeOptionHandler typeOptionHandler;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public ColumnTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("カラム情報")); // RESOURCE
			
			jiemamy = tableModel.getJiemamy();
			columns = tableModel.getColumns();
			
			try {
				dialect = rootModel.findDialect();
			} catch (ClassNotFoundException e) {
				// TODO GeneriDialectをセットするように
				dialect = JiemamyCorePlugin.getDialectResolver().getAllInstance().get(0);
				logger.warn("Dialectのロスト", e);
			}
			
			assert jiemamy != null;
			assert columns != null;
			assert dialect != null;
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			for (DataTypeMold<?> dataTypeMold : allTypes) {
				cmbDataType.add(dataTypeMold.getName());
			}
			
			txtColumnName.addFocusListener(new TextSelectionAdapter(txtColumnName));
			txtColumnName.addKeyListener(editListener);
			
			txtColumnLogicalName.addFocusListener(new TextSelectionAdapter(txtColumnLogicalName));
			txtColumnLogicalName.addKeyListener(editListener);
			
			txtCheckName.addFocusListener(new TextSelectionAdapter(txtCheckName));
			txtCheckName.addKeyListener(editListener);
			
			txtCheckExpression.addFocusListener(new TextSelectionAdapter(txtCheckExpression));
			txtCheckExpression.addKeyListener(editListener);
			
			cmbDataType.addSelectionListener(editListener);
			cmbDataType.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					Table table = getTableViewer().getTable();
					int index = table.getSelectionIndex();
					if (index < 0 || index >= table.getItemCount()) {
						return;
					}
					
					ColumnModel columnModel = (ColumnModel) getTableViewer().getElementAt(index);
					TypeOptionManager typeOptionManager = typeOptionManagers.get(columnModel);
					DataTypeMold<?> dataTypeMold = allTypes.get(cmbDataType.getSelectionIndex());
					if (dataTypeMold instanceof BuiltinDataTypeMold) {
						BuiltinDataTypeMold builtinDataTypeMold = (BuiltinDataTypeMold) dataTypeMold;
						typeOptionManager.createTypeOptionControl(builtinDataTypeMold.getSupportedAdapterClasses());
					} else {
						typeOptionManager.clearTypeOptionControl();
					}
				}
			});
			
			chkIsPK.addSelectionListener(editListener);
			
			chkIsNotNull.addSelectionListener(editListener);
			
			chkIsDisabled.addSelectionListener(editListener);
			
//			chkIsRepresentation.addSelectionListener(editListener);
			
			txtDefaultValue.addFocusListener(new TextSelectionAdapter(txtDefaultValue));
			txtDefaultValue.addKeyListener(editListener);
			
			txtDescription.addFocusListener(new TextSelectionAdapter(txtDefaultValue));
			txtDescription.addKeyListener(editListener);
		}
		
		// THINK ↓要る？
//		@Override
//		protected void configureTable(final Table table) {
//			super.configureTable(table);
//			
//			final Menu menu = new Menu(table);
//			table.setMenu(menu);
//			menu.addMenuListener(new MenuAdapter() {
//				
//				@Override
//				public void menuShown(MenuEvent evt) {
//					for (MenuItem item : menu.getItems()) {
//						item.dispose();
//					}
//					int index = table.getSelectionIndex();
//					if (index == -1) {
//						return;
//					}
//					
//					MenuItem removeItem = new MenuItem(menu, SWT.PUSH);
//					removeItem.setText("&Remove"); // RESOURCE
//					removeItem.addSelectionListener(new SelectionAdapter() {
//						
//						@Override
//						public void widgetSelected(SelectionEvent evt) {
//							removeTableSelectionItem();
//						}
//					});
//				}
//			});
//		}
		
		@Override
		protected void configureTableViewer(TableViewer tableViewer) {
			tableViewer.setLabelProvider(new ColumnLabelProvider());
			final ColumnContentProvider contentProvider = new ColumnContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(columns);
			tableViewer.addFilter(new ViewerFilter() {
				
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return element instanceof ColumnModel;
				}
				
			});
			
			final EventBroker eventBroker = jiemamy.getEventBroker();
			eventBroker.addListener(contentProvider);
			
			// THINK んーーー？？ このタイミングか？
			tableViewer.getTable().addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					eventBroker.removeListener(contentProvider);
				}
				
			});
			
			ExtensionResolver<Dialect> dialectResolver = JiemamyCorePlugin.getDialectResolver();
			IConfigurationElement dialectElement =
					dialectResolver.getExtensionConfigurationElements().get(rootModel.getDialectClassName());
			IConfigurationElement[] children = dialectElement.getChildren("typeOptionHandler");
			if (ArrayUtils.isEmpty(children) == false) {
				try {
					typeOptionHandler = (TypeOptionHandler) children[0].createExecutableExtension("class");
				} catch (Exception e) {
					ExceptionHandler.handleException(e);
				}
			}
			
			typeOptionManagers.clear();
			for (ColumnModel columnModel : columns) {
				TypeOptionManager typeOptionManager =
						new TypeOptionManager(columnModel, cmpTypeOption, editListener, typeOptionHandler);
				typeOptionManagers.put(columnModel, typeOptionManager);
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
			
//			chkIsRepresentation = new Button(cmpChecks, SWT.CHECK);
//			chkIsRepresentation.setText("代表"); // RESOURCE
			
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
			
			TableColumn colCheck = new TableColumn(table, SWT.LEFT);
			colCheck.setText("CHK"); // RESOURCE
			colCheck.setWidth(COL_WIDTH_CHECK);
		}
		
		@Override
		protected void disableEditorControls() {
			txtColumnName.setText(StringUtils.EMPTY);
			txtColumnLogicalName.setText(StringUtils.EMPTY);
			txtCheckName.setText(StringUtils.EMPTY);
			txtCheckExpression.setText(StringUtils.EMPTY);
			cmbDataType.setText(StringUtils.EMPTY);
			chkIsPK.setSelection(false);
			chkIsNotNull.setSelection(false);
			chkIsDisabled.setSelection(false);
//			chkIsRepresentation.setSelection(false);
			txtDefaultValue.setText(StringUtils.EMPTY);
			txtDescription.setText(StringUtils.EMPTY);
			
			txtColumnName.setEnabled(false);
			txtColumnLogicalName.setEnabled(false);
			txtCheckName.setEnabled(false);
			txtCheckExpression.setEnabled(false);
			cmbDataType.setEnabled(false);
			chkIsPK.setEnabled(false);
			chkIsNotNull.setEnabled(false);
			chkIsDisabled.setEnabled(false);
//			chkIsRepresentation.setEnabled(false);
			txtDefaultValue.setEnabled(false);
			txtDescription.setEnabled(false);
			
			for (Control control : cmpTypeOption.getChildren()) {
				control.dispose();
			}
		}
		
		@Override
		protected void enableEditorControls(int index) {
			ColumnModel columnModel = (ColumnModel) getTableViewer().getElementAt(index);
			
			txtColumnName.setEnabled(true);
			txtColumnLogicalName.setEnabled(true);
			txtCheckName.setEnabled(true);
			txtCheckExpression.setEnabled(true);
			cmbDataType.setEnabled(true);
			txtDefaultValue.setEnabled(true);
			txtDescription.setEnabled(true);
			chkIsPK.setEnabled(true);
			chkIsNotNull.setEnabled(true);
			chkIsDisabled.setEnabled(true);
//			chkIsRepresentation.setEnabled(true);
			
			DataType dataType = columnModel.getDataType();
			if (dataType instanceof BuiltinDataType) {
				List<Object> adapters = ((BuiltinDataType) dataType).getAdapters();
				ArrayList<Class<?>> adapterClasses = CollectionsUtil.newArrayList();
				for (Object adapter : adapters) {
					adapterClasses.add(adapter.getClass());
				}
				typeOptionManagers.get(columnModel).createTypeOptionControl(adapterClasses);
			} else {
				for (Control control : cmpTypeOption.getChildren()) {
					control.dispose();
				}
			}
			
			// 現在値の設定
			txtColumnName.setText(columnModel.getName());
			txtColumnLogicalName.setText(JiemamyPropertyUtil.careNull(columnModel.getLogicalName())); // TODO use care
			
			ColumnCheckConstraint columnCheckConstraint = columnModel.getCheckConstraint();
			if (columnCheckConstraint == null) {
				txtCheckName.setText(StringUtils.EMPTY);
				txtCheckExpression.setText(StringUtils.EMPTY);
			} else {
				txtCheckName.setText(JiemamyPropertyUtil.careNull(columnCheckConstraint.getName()));
				txtCheckExpression.setText(JiemamyPropertyUtil.careNull(columnCheckConstraint.getExpression()));
			}
			
			chkIsNotNull.setSelection(columnModel.getNotNullConstraint() != null);
			
			if (dataType instanceof DomainRef) {
				DomainRef domainRef = (DomainRef) dataType;
				DomainModel domainModel = jiemamy.getReferenceResolver().resolve(domainRef);
				cmbDataType.setText(domainModel.getName());
			} else if (dataType instanceof BuiltinDataType) {
				BuiltinDataType builtinDataType = (BuiltinDataType) dataType;
				cmbDataType.setText(builtinDataType.getTypeName());
				if (builtinDataType.hasAdapter(SizedDataTypeAdapter.class)) {
					typeOptionManagers.get(columnModel).setValue(SizedDataTypeAdapter.class);
				}
				if (builtinDataType.hasAdapter(PrecisionedDataTypeAdapter.class)) {
					typeOptionManagers.get(columnModel).setValue(PrecisionedDataTypeAdapter.class);
				}
				if (builtinDataType.hasAdapter(TimezonedDataTypeAdapter.class)) {
					typeOptionManagers.get(columnModel).setValue(TimezonedDataTypeAdapter.class);
				}
			}
			txtDefaultValue.setText(JiemamyPropertyUtil.careNull(columnModel.getDefaultValue()));
			txtDescription.setText(JiemamyPropertyUtil.careNull(columnModel.getDescription()));
			
			chkIsPK.setSelection(columnModel.checkPrimaryKeyColumn());
			
			if (columnModel.hasAdapter(Disablable.class)
					&& Boolean.TRUE.equals(columnModel.getAdapter(Disablable.class).isDisabled())) {
				chkIsDisabled.setSelection(true);
			} else {
				chkIsDisabled.setSelection(false);
			}
			
//			chkIsTypical.setSelection(column.getConstraint(DefinitionModel.CONSTRAINT_TYPICAL));
		}
		
		@Override
		protected JiemamyEntity performAddItem() {
			Table table = getTableViewer().getTable();
			JiemamyFactory factory = jiemamy.getFactory();
			ColumnModel columnModel = factory.newModel(ColumnModel.class);
			
			String newName = "COLUMN_" + (tableModel.getColumns().size() + 1);
			jiemamyFacade.changeModelProperty(columnModel, AttributeProperty.name, newName);
			
			DataType builtinDataType = factory.newDataType(allTypes.get(0));
			jiemamyFacade.changeModelProperty(columnModel, ColumnProperty.dataType, builtinDataType);
			
			jiemamyFacade.addAttribute(tableModel, columnModel);
			
			TypeOptionManager typeOptionManager =
					new TypeOptionManager(columnModel, cmpTypeOption, editListener, typeOptionHandler);
			typeOptionManagers.put(columnModel, typeOptionManager);
			
			int addedIndex = tableModel.getColumns().indexOf(columnModel);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtColumnName.setFocus();
			
			return columnModel;
		}
		
		@Override
		protected JiemamyEntity performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			
			JiemamyFactory factory = jiemamy.getFactory();
			ColumnModel columnModel = factory.newModel(ColumnModel.class);
			String newName = "COLUMN_" + (tableModel.getColumns().size() + 1);
			jiemamyFacade.changeModelProperty(columnModel, AttributeProperty.name, newName);
			
			DataType builtinDataType = factory.newDataType(allTypes.get(0));
			jiemamyFacade.changeModelProperty(columnModel, ColumnProperty.dataType, builtinDataType);
			
			if (index < 0 || index > table.getItemCount()) {
				jiemamyFacade.addAttribute(tableModel, columnModel);
			} else {
				ColumnModel subject = (ColumnModel) getTableViewer().getElementAt(index);
				int subjectIndex = columns.indexOf(subject);
				jiemamyFacade.addAttribute(tableModel, subjectIndex, columnModel);
			}
			
			TypeOptionManager typeOptionManager =
					new TypeOptionManager(columnModel, cmpTypeOption, editListener, typeOptionHandler);
			typeOptionManagers.put(columnModel, typeOptionManager);
			
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
			
			Object subject = getTableViewer().getElementAt(index);
			Object object = getTableViewer().getElementAt(index + 1);
			
			int subjectIndex = tableModel.getAttributes().indexOf(subject);
			int objectIndex = tableModel.getAttributes().indexOf(object);
			
			jiemamyFacade.swapListElement(tableModel, tableModel.getAttributes(), subjectIndex, objectIndex);
			
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
			
			Object subject = getTableViewer().getElementAt(index);
			Object object = getTableViewer().getElementAt(index - 1);
			
			int subjectIndex = tableModel.getAttributes().indexOf(subject);
			int objectIndex = tableModel.getAttributes().indexOf(object);
			
			jiemamyFacade.swapListElement(tableModel, tableModel.getAttributes(), subjectIndex, objectIndex);
			
			table.setSelection(index - 1);
			enableEditControls(index - 1);
		}
		
		@Override
		protected JiemamyEntity performRemoveItem() {
			TableViewer tableViewer = getTableViewer();
			Table table = tableViewer.getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index > table.getItemCount()) {
				return null;
			}
			
			ColumnModel subject = (ColumnModel) getTableViewer().getElementAt(index);
			jiemamyFacade.removeAttribute(tableModel, subject);
			
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
//			ExpandBar expandBar = new ExpandBar(parent, SWT.V_SCROLL);
//			expandBar.setSpacing(8);
//			expandBar.setBackground(ColorConstants.lightGray);
//			gd = new GridData(GridData.FILL_HORIZONTAL);
//			gd.horizontalSpan = 4;
//			expandBar.setLayoutData(gd);
//			layout = new GridLayout(1, false);
//			layout.marginHeight = 0;
//			layout.marginWidth = 0;
//			expandBar.setLayout(layout);
//			ExpandItem expAdvanced = new ExpandItem(expandBar, SWT.NULL);
//			expAdvanced.setText("高度な設定"); // RESOURCE
			Group cmpAdvanced = new Group(parent, SWT.NULL);
			cmpAdvanced.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			layout = new GridLayout(4, false);
//			layout.marginHeight = 0;
//			layout.marginWidth = 0;
			cmpAdvanced.setLayout(layout);
			cmpAdvanced.setText("高度な設定"); // RESOURCE
//			expAdvanced.setControl(cmpAdvanced);
//			expAdvanced.setHeight(cmpAdvanced.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			
			label = new Label(cmpAdvanced, SWT.NULL);
			label.setText("制約名(&M)"); // RESOURCE
			
			txtCheckName = new Text(cmpAdvanced, SWT.BORDER);
			txtCheckName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpAdvanced, SWT.NULL);
			label.setText("チェック制約式(&K)"); // RESOURCE
			
			txtCheckExpression = new Text(cmpAdvanced, SWT.BORDER);
			txtCheckExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpAdvanced, SWT.NULL);
			label.setText("デフォルト値(&F)"); // RESOURCE
			
			txtDefaultValue = new Text(cmpAdvanced, SWT.BORDER);
			txtDefaultValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpAdvanced, SWT.NULL);
			label.setText("説明(&D)"); // RESOURCE
			
			txtDescription = new Text(cmpAdvanced, SWT.MULTI | SWT.BORDER);
			txtDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		private void updateModel() {
			int columnEditIndex = getTableViewer().getTable().getSelectionIndex();
			int selectionInedx = cmbDataType.getSelectionIndex();
			
			if (columnEditIndex == -1 || selectionInedx == -1) {
				return;
			}
			
			ColumnModel columnModel = tableModel.getColumns().get(columnEditIndex);
			
			JiemamyFactory factory = columnModel.getJiemamy().getFactory();
			
			String columnName = JiemamyPropertyUtil.careNull(txtColumnName.getText(), false);
			jiemamyFacade.changeModelProperty(columnModel, AttributeProperty.name, columnName);
			
			String logicalName = JiemamyPropertyUtil.careNull(txtColumnLogicalName.getText(), true);
			jiemamyFacade.changeModelProperty(columnModel, AttributeProperty.logicalName, logicalName);
			
			DataType dataType = factory.newDataType(allTypes.get(cmbDataType.getSelectionIndex()));
			jiemamyFacade.changeModelProperty(columnModel, ColumnProperty.dataType, dataType);
			
			String defaultValue = JiemamyPropertyUtil.careNull(txtDefaultValue.getText(), true);
			jiemamyFacade.changeModelProperty(columnModel, ColumnProperty.defaultValue, defaultValue);
			
			String description = JiemamyPropertyUtil.careNull(txtDescription.getText(), true);
			jiemamyFacade.changeModelProperty(columnModel, AttributeProperty.description, description);
			
			if (chkIsNotNull.getSelection() == false) {
				jiemamyFacade.changeModelProperty(columnModel, ColumnProperty.notNullConstraint, null);
			} else if (columnModel.getNotNullConstraint() == null) {
				NotNullConstraint nnConstraint = factory.newModel(NotNullConstraint.class);
				jiemamyFacade.changeModelProperty(columnModel, ColumnProperty.notNullConstraint, nnConstraint);
			}
			
			PrimaryKeyConstraintModel primaryKey = null;
			try {
				primaryKey = tableModel.getPrimaryKey();
			} catch (ElementNotFoundException e) {
				// ignore
			}
			if (chkIsPK.getSelection() == false) {
				if (primaryKey != null) {
					jiemamyFacade.removeKeyColumn(primaryKey, columnModel);
				}
			} else {
				if (primaryKey == null) {
					primaryKey = factory.newModel(PrimaryKey.class);
					jiemamyFacade.addAttribute(tableModel, primaryKey);
					rootModel.normalize();
				}
				jiemamyFacade.addKeyColumn(primaryKey, columnModel);
			}
			
			if (StringUtils.isEmpty(txtCheckName.getText()) && StringUtils.isEmpty(txtCheckExpression.getText())) {
				jiemamyFacade.changeModelProperty(columnModel, ColumnProperty.checkConstraint, null);
			} else {
				ColumnCheckConstraint checkConstraint = columnModel.getCheckConstraint();
				if (checkConstraint == null) {
					checkConstraint = factory.newModel(ColumnCheckConstraint.class);
					jiemamyFacade.changeModelProperty(columnModel, ColumnProperty.checkConstraint, checkConstraint);
				}
				String checkName = JiemamyPropertyUtil.careNull(txtCheckName.getText(), true);
				jiemamyFacade.changeModelProperty(checkConstraint, ConstraintProperty.name, checkName);
				
				String expression = JiemamyPropertyUtil.careNull(txtCheckExpression.getText(), false);
				jiemamyFacade.changeModelProperty(checkConstraint, CheckConstraintProperty.expression, expression);
			}
			
			if (chkIsDisabled.getSelection() == false) {
				if (columnModel.hasAdapter(Disablable.class)) {
					columnModel.unregisterAdapter(Disablable.class);
				}
			} else {
				if (columnModel.hasAdapter(Disablable.class) == false) {
					columnModel.registerAdapter(factory.newAdapter(Disablable.class));
				}
				columnModel.getAdapter(Disablable.class).setDisabled(true);
			}
			
//			RepresentationAdapter representationAdapter = tableModel.getAdapter(RepresentationAdapter.class);
//			if (chkIsRepresentation.getSelection()) {
//				representationAdapter.setRepresentation(true);
//			} else {
//				representationAdapter.setRepresentation(null);
			//			}
			
			typeOptionManagers.get(columnModel).writeBackToAdapter();
		}
		

		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
				columnTableEditor.refreshTable();
			}
		}
	}
}
