/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/03/17
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
package org.jiemamy.eclipse.core.ui.composer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.JiemamyCorePlugin;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.eclipse.extension.ExtensionResolver;
import org.jiemamy.utils.sql.DriverNotFoundException;
import org.jiemamy.utils.sql.DriverUtil;

/**
 * データベースインポートに関する設定を行うウィザードページ。
 * 
 * @author daisuke
 */
class DatabaseImportWizardPage extends WizardPage {
	
	protected static final String[] JAR_EXTENSIONS = new String[] {
		"*.jar",
		"*.*"
	};
	
	private Combo cmbDialect;
	
	private org.eclipse.swt.widgets.List lstDriverJars;
	
	private Button btnAddJar;
	
	private Button btnRemoveJar;
	
	private Combo cmbDriverClass;
	
	private Text txtUri;
	
	private Text txtUsername;
	
	private Text txtPassword;
	
	private Text txtSchema;
	
	private Button btnImportDataSet;
	
	private Button btnTest;
	
	private ExtensionResolver<Dialect> dialectResolver;
	
	private final IDialogSettings settings;
	

	/**
	 * インスタンスを生成する。
	 * @param settings ダイアログセッティング
	 */
	DatabaseImportWizardPage(IDialogSettings settings) {
		super(Messages.DatabaseImportWizardPage_title, Messages.DatabaseImportWizardPage_title, (ImageDescriptor) null);
		dialectResolver = JiemamyCorePlugin.getDialectResolver();
		setPageComplete(false);
		this.settings = settings;
	}
	
	public void createControl(final Composite parent) { // CHECKSTYLE IGNORE THIS LINE
		Label label;
		GridData gd;
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.DatabaseImportWizardPage_label_dbType);
		
		cmbDialect = new Combo(composite, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		cmbDialect.setLayoutData(gd);
		for (Dialect dialect : dialectResolver.getAllInstance()) {
			cmbDialect.add(dialect.toString());
		}
		cmbDialect.select(0);
		cmbDialect.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String connectionUriTemplate = getDialect().getConnectionUriTemplate();
				txtUri.setText(StringUtils.defaultIfEmpty(connectionUriTemplate, txtUri.getText()));
			}
		});
		// THINK JiemamyContextに設定されたDialectを設定する?
//		cmbDialect.setText(rootModel.getDialectClassName());
		cmbDialect.setText(StringUtils.defaultIfEmpty(settings.get("cmbDialect"), ""));
		
		label = new Label(composite, SWT.NONE);
		label.setText("JDBCドライバjar(&J)"); // RESOURCE
		
		lstDriverJars = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.MULTI);
		lstDriverJars.setLayoutData(new GridData(GridData.FILL_BOTH));
		String pathsString = StringUtils.defaultIfEmpty(settings.get("lstDriverJars"), "");
		for (String path : pathsString.split(File.pathSeparator)) {
			if (new File(path).exists()) {
				lstDriverJars.add(path);
			}
		}
		
		Composite cmpButtons = new Composite(composite, SWT.NULL);
		cmpButtons.setLayout(new RowLayout(SWT.VERTICAL));
		createButtons(cmpButtons);
		
		label = new Label(composite, SWT.NONE);
		label.setText("JDBCドライバクラス(&C)"); // RESOURCE
		
		cmbDriverClass = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		cmbDriverClass.setLayoutData(gd);
		if (lstDriverJars.getItemCount() > 0) {
			driverListChanged();
		}
		cmbDriverClass.setText(StringUtils.defaultIfEmpty(settings.get("cmbDriverClass"), ""));
		
		label = new Label(composite, SWT.NONE);
		label.setText("接続URI(&I)"); // RESOURCE
		
		txtUri = new Text(composite, SWT.BORDER);
		txtUri.addFocusListener(new TextSelectionAdapter(txtUri));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		txtUri.setLayoutData(gd);
		txtUri.setText(StringUtils.defaultIfEmpty(settings.get("txtUri"), ""));
		
		label = new Label(composite, SWT.NONE);
		label.setText("接続ユーザ名(&U)"); // RESOURCE
		
		txtUsername = new Text(composite, SWT.BORDER);
		txtUsername.addFocusListener(new TextSelectionAdapter(txtUsername));
		txtUsername.setText("sa");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		txtUsername.setLayoutData(gd);
		txtUsername.setText(StringUtils.defaultIfEmpty(settings.get("txtUsername"), ""));
		
		label = new Label(composite, SWT.NONE);
		label.setText("接続パスワード(&P)"); // RESOURCE
		
		txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		txtPassword.addFocusListener(new TextSelectionAdapter(txtPassword));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		txtPassword.setLayoutData(gd);
		txtPassword.setText(StringUtils.defaultIfEmpty(settings.get("txtPassword"), ""));
		
		label = new Label(composite, SWT.NONE);
		label.setText("スキーマ名(&S)"); // RESOURCE
		
		txtSchema = new Text(composite, SWT.BORDER);
		txtSchema.addFocusListener(new TextSelectionAdapter(txtSchema));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		txtSchema.setLayoutData(gd);
		txtSchema.setText(StringUtils.defaultIfEmpty(settings.get("txtSchema"), ""));
		
		new Label(composite, SWT.NONE); // dummy
		
		btnImportDataSet = new Button(composite, SWT.CHECK);
		btnImportDataSet.setText("DataSetをインポートする"); // RESOURCE
		btnImportDataSet.setEnabled(false); // TODO 現在サポートしていない
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		btnImportDataSet.setLayoutData(gd);
		btnImportDataSet.setSelection(settings.getBoolean("btnImportDataSet"));
		
		createTestButton(composite);
		setControl(composite);
	}
	
	/**
	 * SQL方言を取得する。
	 * 
	 * @return SQL方言
	 */
	public Dialect getDialect() {
		Dialect instance;
		try {
			instance = dialectResolver.getInstance(cmbDialect.getText());
		} catch (CoreException e) {
			instance = dialectResolver.getAllInstance().get(0);
		}
		return instance;
	}
	
	/**
	 * ドライバの完全修飾クラス名を取得する。
	 * 
	 * @return ドライバの完全修飾クラス名
	 */
	public String getDriverClassName() {
		return cmbDriverClass.getText();
	}
	
	/**
	 * ドライバJARファイルのパス配列を取得する。
	 * 
	 * @return ドライバJARファイルのパス配列
	 */
	public URL[] getDriverJarPaths() {
		String[] paths = lstDriverJars.getItems();
		List<URL> result = Lists.newArrayListWithCapacity(paths.length);
		for (String path : paths) {
			try {
				File file = new File(path.replace(" ", "%20")); // HACK %20置換とかするんじゃないｗ
				result.add(file.toURI().toURL());
			} catch (MalformedURLException e) {
				ExceptionHandler.handleException(e);
			}
		}
		return result.toArray(new URL[paths.length]);
	}
	
	/**
	 * 接続パスワードを取得する。
	 * 
	 * @return 接続パスワード
	 */
	public String getPassword() {
		return txtPassword.getText();
	}
	
	/**
	 * スキーマ名を取得する。
	 * 
	 * @return スキーマ名
	 */
	public String getSchema() {
		return txtSchema.getText();
	}
	
	/**
	 * 接続URIを取得する。
	 * 
	 * @return 接続URI
	 */
	public String getUri() {
		return txtUri.getText();
	}
	
	/**
	 * 接続ユーザ名を取得する。
	 * 
	 * @return 接続ユーザ名
	 */
	public String getUsername() {
		return txtUsername.getText();
	}
	
	/**
	 * テーブルのコンテンツをデータセットとしてインポートするかどうかを取得する。
	 * 
	 * @return テーブルのコンテンツをデータセットとしてインポートするかどうか
	 */
	public boolean isImportDataSet() {
		return btnImportDataSet.getSelection();
	}
	
	private void connectionSettingReconfigure() {
		setPageComplete(false);
		
		cmbDialect.setEnabled(true);
		lstDriverJars.setEnabled(true);
		btnAddJar.setEnabled(true);
		btnRemoveJar.setEnabled(true);
		cmbDriverClass.setEnabled(true);
		txtUri.setEnabled(true);
		txtUsername.setEnabled(true);
		txtPassword.setEnabled(true);
		txtSchema.setEnabled(true);
//		btnImportDataSet.setEnabled(true);
		
		btnTest.setText(Messages.DatabaseImportWizardPage_btn_connectionTest);
	}
	
	private void connectionSucceeded() {
		btnTest.setText(Messages.DatabaseImportWizardPage_btn_reconfigure);
		
		cmbDialect.setEnabled(false);
		lstDriverJars.setEnabled(false);
		btnAddJar.setEnabled(false);
		btnRemoveJar.setEnabled(false);
		cmbDriverClass.setEnabled(false);
		txtUri.setEnabled(false);
		txtUsername.setEnabled(false);
		txtPassword.setEnabled(false);
		txtSchema.setEnabled(false);
		btnImportDataSet.setEnabled(false);
		
		settings.put("cmbDialect", cmbDialect.getText());
		settings.put("lstDriverJars", StringUtils.join(lstDriverJars.getItems(), File.pathSeparator));
		settings.put("cmbDriverClass", cmbDriverClass.getText());
		settings.put("txtUri", txtUri.getText());
		settings.put("txtUsername", txtUsername.getText());
		settings.put("txtPassword", txtPassword.getText());
		settings.put("txtSchema", txtSchema.getText());
		settings.put("btnImportDataSet", btnImportDataSet.getSelection());
		
		setPageComplete(true);
	}
	
	private void createButtons(Composite cmpButtons) {
		btnAddJar = new Button(cmpButtons, SWT.PUSH);
		btnAddJar.setText("追加(&A)"); // RESOURCE
		btnAddJar.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.MULTI | SWT.OPEN);
				fileDialog.setText("JDBCドライバjarの選択"); // RESOURCE
				fileDialog.setFilterExtensions(JAR_EXTENSIONS);
				if (fileDialog.open() == null) {
					return;
				}
				String[] fileNames = fileDialog.getFileNames();
				String filterPath = fileDialog.getFilterPath();
				for (String fileName : fileNames) {
					lstDriverJars.add(filterPath + SystemUtils.FILE_SEPARATOR + fileName);
				}
				if (ArrayUtils.isEmpty(fileNames) == false) {
					driverListChanged();
				}
			}
		});
		btnRemoveJar = new Button(cmpButtons, SWT.PUSH);
		btnRemoveJar.setText("削除(&R)"); // RESOURCE
		btnRemoveJar.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int[] selectionIndices = lstDriverJars.getSelectionIndices();
				if (ArrayUtils.isEmpty(selectionIndices)) {
					return;
				}
				Arrays.sort(selectionIndices);
				ArrayUtils.reverse(selectionIndices);
				for (int selectionIndex : selectionIndices) {
					lstDriverJars.remove(selectionIndex);
				}
				if (ArrayUtils.isEmpty(selectionIndices) == false) {
					driverListChanged();
				}
			}
		});
	}
	
	/**
	 * 接続テストボタンを生成する。
	 * 
	 * @param composite 親コンポーネント
	 */
	private void createTestButton(Composite composite) {
		btnTest = new Button(composite, SWT.PUSH);
		btnTest.setText(Messages.DatabaseImportWizardPage_btn_connectionTest);
		btnTest.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnTest.getText().equals(Messages.DatabaseImportWizardPage_btn_reconfigure)) {
					connectionSettingReconfigure();
				} else {
					testConnection();
				}
			}
		});
	}
	
	/**
	 * ドライバjarのリストが変更された時のハンドラメソッド。
	 */
	private void driverListChanged() {
		String oldSelection = cmbDriverClass.getText();
		cmbDriverClass.removeAll();
		try {
			Collection<Class<? extends Driver>> driverClasses = DriverUtil.getDriverClasses(getDriverJarPaths());
			for (Class<? extends Driver> clazz : driverClasses) {
				String className = clazz.getName();
				cmbDriverClass.add(className);
				if (className.equals(oldSelection)) {
					cmbDriverClass.setText(className);
				}
			}
		} catch (IOException e) {
			ExceptionHandler.handleException(e);
		}
		if (StringUtils.isEmpty(cmbDriverClass.getText())) {
			cmbDriverClass.select(0); // Indices that are out of range are ignored. なのでOK
		}
		setPageComplete(false);
	}
	
	/**
	 * 接続のテストを行う。
	 */
	private void testConnection() {
		Connection connection = null;
		try {
			Driver driver = DriverUtil.getDriverInstance(getDriverJarPaths(), getDriverClassName());
			Properties info = new Properties();
			info.setProperty("user", getUsername());
			info.setProperty("password", getPassword());
			connection = driver.connect(getUri(), info);
			if (connection != null) {
				MessageDialog.openInformation(getShell(), "接続成功", "データベースに接続できました。"); // RESOURCE
				connectionSucceeded();
			} else {
				MessageDialog.openError(getShell(), "接続失敗0", "null connection"); // RESOURCE
			}
		} catch (DriverNotFoundException ex) {
			MessageDialog.openError(getShell(), "接続失敗1", ex.getClass().getName() + " " + ex.getMessage()); // RESOURCE
		} catch (InstantiationException ex) {
			MessageDialog.openError(getShell(), "接続失敗2", ex.getClass().getName() + " " + ex.getMessage()); // RESOURCE
		} catch (IllegalAccessException ex) {
			MessageDialog.openError(getShell(), "接続失敗3", ex.getClass().getName() + " " + ex.getMessage()); // RESOURCE
		} catch (IOException ex) {
			MessageDialog.openError(getShell(), "接続失敗4", ex.getClass().getName() + " " + ex.getMessage()); // RESOURCE
		} catch (SQLException ex) {
			MessageDialog.openError(getShell(), "接続失敗5", ex.getClass().getName() + " " + ex.getMessage()); // RESOURCE
		} catch (Exception ex) {
			MessageDialog.openError(getShell(), "接続失敗6", ex.getClass().getName() + " " + ex.getMessage()); // RESOURCE
		} finally {
			DbUtils.closeQuietly(connection);
		}
	}
}
