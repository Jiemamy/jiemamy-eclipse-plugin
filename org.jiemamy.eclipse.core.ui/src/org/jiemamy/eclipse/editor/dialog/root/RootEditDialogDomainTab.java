/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/02/16
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
package org.jiemamy.eclipse.editor.dialog.root;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
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
import org.jiemamy.model.dbo.DomainModel;
import org.jiemamy.transaction.CommandListener;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.utils.LogMarker;
import org.jiemamy.utils.collection.CollectionsUtil;

/**
 * データベース編集ダイアログの「ドメイン」タブ。
 * 
 * @author daisuke
 */
public class RootEditDialogDomainTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(RootEditDialogDomainTab.class);
	
	private final JiemamyContext rootModel;
	
	private List<BuiltinDataTypeMold> allTypes;
	
	private AbstractTableEditor domainTableEditor;
	
	private final JiemamyFacade jiemamyFacade;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param rootModel 編集対象{@link JiemamyContext}
	 * @param jiemamyFacade モデル操作を行うファサード
	 */
	public RootEditDialogDomainTab(TabFolder parentTabFolder, int style, JiemamyContext rootModel,
			JiemamyFacade jiemamyFacade) {
		super(parentTabFolder, style, Messages.Tab_Domains);
		
		this.rootModel = rootModel;
		this.jiemamyFacade = jiemamyFacade;
		
		Dialect dialect;
		try {
			dialect = rootModel.findDialect();
		} catch (ClassNotFoundException e) {
			dialect = JiemamyCorePlugin.getDialectResolver().getAllInstance().get(0);
			logger.warn("Dialectのロスト", e);
		}
		
		allTypes = CollectionsUtil.newArrayList(dialect.getAllDataTypes().size());
		
		allTypes.addAll(dialect.getAllDataTypes());
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		domainTableEditor = new DomainTableEditor(composite, SWT.NULL);
		domainTableEditor.configure();
		domainTableEditor.disableEditControls();
		
		getTabItem().setControl(composite);
	}
	
	@Override
	public boolean isTabComplete() {
		// TODO Auto-generated method stub
		return true;
	}
	

	/**
	 * Domain用ContentProvider実装クラス。
	 * 
	 * @author daisuke
	 */
	private class DomainContentProvider extends ArrayContentProvider implements CommandListener {
		
		private Viewer viewer;
		

		public void commandExecuted(Command command) {
			logger.debug(LogMarker.LIFECYCLE, "DomainContentProvider: commandExecuted");
			domainTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		@Override
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "DomainContentProvider: disposed");
			super.dispose();
		}
		
		public JiemamyElement getTargetModel() {
			return (JiemamyElement) viewer.getInput();
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "DomainContentProvider: input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
			
			this.viewer = viewer;
			
			super.inputChanged(viewer, oldInput, newInput);
		}
		
	}
	
	/**
	 * Domain用LabelProviderの実装クラス。
	 * 
	 * @author daisuke
	 */
	private class DomainLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
			DomainModel domainModel = (DomainModel) element;
			
			switch (columnIndex) {
				case 2:
					return ir.get(domainModel.getNotNullConstraint() != null ? Images.CHECK_ON : Images.CHECK_OFF);
					
				case 3:
					ColumnCheckConstraint check = domainModel.getCheckConstraint();
					return ir.get(check != null && StringUtils.isEmpty(check.getExpression()) == false
							? Images.CHECK_ON : Images.CHECK_OFF);
					
				default:
					return null;
			}
		}
		
		public String getColumnText(Object element, int columnIndex) {
			DomainModel domainModel = (DomainModel) element;
			switch (columnIndex) {
				case 0:
					return LabelStringUtil.getString(rootModel, domainModel, DisplayPlace.TABLE);
					
				case 1:
					return LabelStringUtil.getString(rootModel, domainModel.getDataType(), DisplayPlace.TABLE);
					
				default:
					return StringUtils.EMPTY;
			}
		}
	}
	
	private class DomainTableEditor extends AbstractTableEditor {
		
		private static final int COL_WIDTH_NAME = 100;
		
		private static final int COL_WIDTH_TYPE = 150;
		
		private static final int COL_WIDTH_NN = 80;
		
		private static final int COL_WIDTH_CHECK = 80;
		
		private final EditListener editListener = new EditListenerImpl();
		
		private final Jiemamy jiemamy;
		
		private Dialect dialect;
		
		private Text txtDomainName;
		
		private Combo cmbDataType;
		
		private Text txtCheckName;
		
		private Text txtCheckExpression;
		
		private Button chkIsNotNull;
		
		private Text txtDescription;
		
		private Composite cmpTypeOption;
		
		private Map<DomainModel, TypeOptionManager> typeOptionManagers = CollectionsUtil.newHashMap();
		
		private final List<DomainModel> domains;
		
		private TypeOptionHandler typeOptionHandler;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public DomainTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig(Messages.Label_GroupTitle_Domain));
			
			jiemamy = rootModel.getJiemamy();
			domains = rootModel.getDomains();
			
			try {
				dialect = rootModel.findDialect();
			} catch (ClassNotFoundException e) {
				// TODO GenericDialectをセットするように
				dialect = JiemamyCorePlugin.getDialectResolver().getAllInstance().get(0);
				logger.warn("Dialectのロスト", e);
			}
			
			assert jiemamy != null;
			assert domains != null;
			assert dialect != null;
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			for (BuiltinDataTypeMold typeInfo : allTypes) {
				cmbDataType.add(typeInfo.getName());
			}
			
			txtDomainName.addFocusListener(new TextSelectionAdapter(txtDomainName));
			txtDomainName.addKeyListener(editListener);
			
			cmbDataType.addSelectionListener(editListener);
			cmbDataType.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					Table table = getTableViewer().getTable();
					int index = table.getSelectionIndex();
					if (index < 0 || index >= table.getItemCount()) {
						return;
					}
					
					DomainModel domainModel = (DomainModel) getTableViewer().getElementAt(index);
					TypeOptionManager typeOptionManager = typeOptionManagers.get(domainModel);
					DataTypeMold<?> dataTypeMold = allTypes.get(cmbDataType.getSelectionIndex());
					if (dataTypeMold instanceof BuiltinDataTypeMold) {
						BuiltinDataTypeMold builtinDataTypeMold = (BuiltinDataTypeMold) dataTypeMold;
						typeOptionManager.createTypeOptionControl(builtinDataTypeMold.getSupportedAdapterClasses());
					} else {
						typeOptionManager.clearTypeOptionControl();
					}
				}
			});
			
			chkIsNotNull.addSelectionListener(editListener);
			
			txtCheckName.addFocusListener(new TextSelectionAdapter(txtCheckName));
			txtCheckName.addKeyListener(editListener);
			
			txtCheckExpression.addFocusListener(new TextSelectionAdapter(txtCheckExpression));
			txtCheckExpression.addKeyListener(editListener);
			
			txtDescription.addFocusListener(new TextSelectionAdapter(txtDescription));
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
			tableViewer.setLabelProvider(new DomainLabelProvider());
			final DomainContentProvider contentProvider = new DomainContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(domains);
			
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
			for (DomainModel domainModel : domains) {
				TypeOptionManager typeOptionManager =
						new TypeOptionManager(domainModel, cmpTypeOption, editListener, typeOptionHandler);
				typeOptionManagers.put(domainModel, typeOptionManager);
			}
		}
		
		@Override
		protected void createEditorControls(Composite parent) {
			GridData gd;
			Label label;
			
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout = new GridLayout(4, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			composite.setLayout(layout);
			
			label = new Label(composite, SWT.NULL);
			label.setText(Messages.Label_Domain_Name);
			
			txtDomainName = new Text(composite, SWT.BORDER);
			txtDomainName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(composite, SWT.NULL);
			label.setText(Messages.Label_Domain_Description);
			
			txtDescription = new Text(composite, SWT.MULTI | SWT.BORDER);
			txtDescription.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			label = new Label(composite, SWT.NULL);
			label.setText(Messages.Label_Domain_DataType);
			
			Composite cmpTypes = new Composite(composite, SWT.NULL);
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
			
			label = new Label(composite, SWT.NULL);
			label.setText("制約名"); // RESOURCE
			
			txtCheckName = new Text(composite, SWT.BORDER);
			txtCheckName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(composite, SWT.NULL);
			label.setText(Messages.Label_Domain_CheckConstraint);
			
			txtCheckExpression = new Text(composite, SWT.BORDER);
			txtCheckExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Composite cmpChecks = new Composite(parent, SWT.NULL);
			cmpChecks.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			layout = new GridLayout(1, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpChecks.setLayout(layout);
			
			chkIsNotNull = new Button(cmpChecks, SWT.CHECK);
			chkIsNotNull.setText(Messages.Label_Domain_NotNullConstraint);
		}
		
		@Override
		protected void createTableColumns(Table table) {
			TableColumn colName = new TableColumn(table, SWT.LEFT);
			colName.setText(Messages.Column_Domain_Name);
			colName.setWidth(COL_WIDTH_NAME);
			
			TableColumn colType = new TableColumn(table, SWT.LEFT);
			colType.setText(Messages.Column_Domain_DataType);
			colType.setWidth(COL_WIDTH_TYPE);
			
			TableColumn colNotNull = new TableColumn(table, SWT.LEFT);
			colNotNull.setText(Messages.Column_Domain_NotNullConstraint);
			colNotNull.setWidth(COL_WIDTH_NN);
			
			TableColumn colCheck = new TableColumn(table, SWT.LEFT);
			colCheck.setText(Messages.Column_Domain_CheckConstraint);
			colCheck.setWidth(COL_WIDTH_CHECK);
		}
		
		@Override
		protected void disableEditorControls() {
			txtDomainName.setText(StringUtils.EMPTY);
			cmbDataType.setText(StringUtils.EMPTY);
			txtCheckName.setText(StringUtils.EMPTY);
			txtCheckExpression.setText(StringUtils.EMPTY);
			txtDescription.setText(StringUtils.EMPTY);
			chkIsNotNull.setSelection(false);
			
			txtDomainName.setEnabled(false);
			cmbDataType.setEnabled(false);
			txtCheckName.setEnabled(false);
			txtCheckExpression.setEnabled(false);
			txtDescription.setEnabled(false);
			chkIsNotNull.setEnabled(false);
			
			for (Control control : cmpTypeOption.getChildren()) {
				control.dispose();
			}
		}
		
		@Override
		protected void enableEditorControls(int index) {
			DomainModel domainModel = domains.get(index);
			
			txtDomainName.setEnabled(true);
			cmbDataType.setEnabled(true);
			txtDescription.setEnabled(true);
			chkIsNotNull.setEnabled(true);
			txtCheckName.setEnabled(true);
			txtCheckExpression.setEnabled(true);
			
			BuiltinDataType dataType = domainModel.getDataType();
			List<Object> adapters = dataType.getAdapters();
			List<Class<?>> adapterClasses = CollectionsUtil.newArrayList();
			for (Object adapter : adapters) {
				adapterClasses.add(adapter.getClass());
			}
			typeOptionManagers.get(domainModel).createTypeOptionControl(adapterClasses);
			
			// 現在値の設定
			txtDomainName.setText(domainModel.getName());
			cmbDataType.setText(DataTypeUtil.getTypeName(dataType, jiemamy.getReferenceResolver()));
			txtDescription.setText(JiemamyPropertyUtil.careNull(domainModel.getDescription()));
			chkIsNotNull.setSelection(domainModel.getNotNullConstraint() != null);
			CheckConstraint checkConstraint = domainModel.getCheckConstraint();
			if (checkConstraint == null) {
				txtCheckName.setText(StringUtils.EMPTY);
				txtCheckExpression.setText(StringUtils.EMPTY);
			} else {
				txtCheckName.setText(JiemamyPropertyUtil.careNull(checkConstraint.getName()));
				txtCheckExpression.setText(checkConstraint.getExpression());
			}
			
			cmbDataType.setText(dataType.getTypeName());
			if (dataType.hasAdapter(SizedDataTypeAdapter.class)) {
				typeOptionManagers.get(domainModel).setValue(SizedDataTypeAdapter.class);
			}
			if (dataType.hasAdapter(PrecisionedDataTypeAdapter.class)) {
				typeOptionManagers.get(domainModel).setValue(PrecisionedDataTypeAdapter.class);
			}
			if (dataType.hasAdapter(TimezonedDataTypeAdapter.class)) {
				typeOptionManagers.get(domainModel).setValue(TimezonedDataTypeAdapter.class);
			}
		}
		
		@Override
		protected JiemamyElement performAddItem() {
			Table table = getTableViewer().getTable();
			JiemamyFactory factory = jiemamy.getFactory();
			DomainModel domainModel = factory.newModel(DomainModel.class);
			
			String newName = "DOMAIN_" + (domains.size() + 1);
			jiemamyFacade.changeModelProperty(domainModel, DomainProperty.name, newName);
			
			BuiltinDataType builtinDataType = factory.newDataType(allTypes.get(0));
			jiemamyFacade.changeModelProperty(domainModel, DomainProperty.dataType, builtinDataType);
			
			jiemamyFacade.addDomain(domainModel);
			
			TypeOptionManager typeOptionManager =
					new TypeOptionManager(domainModel, cmpTypeOption, editListener, typeOptionHandler);
			typeOptionManagers.put(domainModel, typeOptionManager);
			
			int addedIndex = domains.indexOf(domainModel);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtDomainName.setFocus();
			
			return domainModel;
		}
		
		@Override
		protected JiemamyElement performInsertItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			
			JiemamyFactory factory = jiemamy.getFactory();
			DomainModel domainModel = factory.newModel(DomainModel.class);
			String newName = "DOMAIN_" + (domains.size() + 1);
			jiemamyFacade.changeModelProperty(domainModel, DomainProperty.name, newName);
			
			BuiltinDataType builtinDataType = factory.newDataType(allTypes.get(0));
			jiemamyFacade.changeModelProperty(domainModel, DomainProperty.dataType, builtinDataType);
			
			if (index < 0 || index > table.getItemCount()) {
				jiemamyFacade.addDomain(domainModel);
			} else {
				jiemamyFacade.addDomain(domainModel, index);
			}
			
			TypeOptionManager typeOptionManager =
					new TypeOptionManager(domainModel, cmpTypeOption, editListener, typeOptionHandler);
			typeOptionManagers.put(domainModel, typeOptionManager);
			
			int addedIndex = domains.indexOf(domainModel);
			table.setSelection(addedIndex);
			enableEditControls(addedIndex);
			txtDomainName.setFocus();
			
			return domainModel;
		}
		
		@Override
		protected void performMoveDownItem() {
			Table table = getTableViewer().getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index >= table.getItemCount()) {
				return;
			}
			
			jiemamyFacade.swapListElement(rootModel, domains, index, index + 1);
			
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
			
			jiemamyFacade.swapListElement(rootModel, domains, index, index - 1);
			
			table.setSelection(index - 1);
			enableEditControls(index - 1);
		}
		
		@Override
		protected JiemamyElement performRemoveItem() {
			TableViewer tableViewer = getTableViewer();
			Table table = tableViewer.getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index > table.getItemCount()) {
				return null;
			}
			
			DomainModel domainModel = domains.get(index);
			jiemamyFacade.removeDomain(domainModel);
			
			tableViewer.remove(domainModel);
			int nextSelection = table.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				table.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			table.setFocus();
			
			typeOptionManagers.remove(domainModel);
			
			return domainModel;
		}
		
		private void updateModel() {
			int domainEditIndex = getTableViewer().getTable().getSelectionIndex();
			int selectionInedx = cmbDataType.getSelectionIndex();
			
			if (domainEditIndex == -1 || selectionInedx == -1) {
				return;
			}
			DomainModel domainModel = domains.get(domainEditIndex);
			
			JiemamyFactory factory = domainModel.getJiemamy().getFactory();
			
			String domainName = JiemamyPropertyUtil.careNull(txtDomainName.getText(), false);
			jiemamyFacade.changeModelProperty(domainModel, DomainProperty.name, domainName);
			
			String description = JiemamyPropertyUtil.careNull(txtDescription.getText(), true);
			jiemamyFacade.changeModelProperty(domainModel, DomainProperty.description, description);
			
			DataType dataType = factory.newDataType(allTypes.get(cmbDataType.getSelectionIndex()));
			jiemamyFacade.changeModelProperty(domainModel, DomainProperty.dataType, dataType);
			
			if (StringUtils.isEmpty(txtCheckName.getText()) && StringUtils.isEmpty(txtCheckExpression.getText())) {
				jiemamyFacade.changeModelProperty(domainModel, DomainProperty.checkConstraint, null);
			} else {
				ColumnCheckConstraint checkConstraint = domainModel.getCheckConstraint();
				if (checkConstraint == null) {
					checkConstraint = factory.newModel(ColumnCheckConstraint.class);
					jiemamyFacade.changeModelProperty(domainModel, DomainProperty.checkConstraint, checkConstraint);
				}
				String checkName = JiemamyPropertyUtil.careNull(txtCheckName.getText(), true);
				jiemamyFacade.changeModelProperty(checkConstraint, ConstraintProperty.name, checkName);
				
				String expression = JiemamyPropertyUtil.careNull(txtCheckExpression.getText(), false);
				jiemamyFacade.changeModelProperty(checkConstraint, CheckConstraintProperty.expression, expression);
			}
			
			if (chkIsNotNull.getSelection() == false) {
				jiemamyFacade.changeModelProperty(domainModel, DomainProperty.notNullConstraint, null);
			} else if (domainModel.getNotNullConstraint() == null) {
				NotNullConstraint nnConstraint = factory.newModel(NotNullConstraint.class);
				jiemamyFacade.changeModelProperty(domainModel, DomainProperty.notNullConstraint, nnConstraint);
			}
			
			typeOptionManagers.get(domainModel).writeBackToAdapter();
		}
		

		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
				domainTableEditor.refreshTable();
			}
		}
	}
}
