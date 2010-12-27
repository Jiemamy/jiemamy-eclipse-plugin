/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/08/03
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
package org.jiemamy.eclipse.editor.command;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;
import org.seasar.eclipse.common.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.model.dbo.ViewModel;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * コネクション作成GEFコマンド。
 * 
 * @author daisuke
 */
public class CreateConnectionCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(CreateConnectionCommand.class);
	
	/** 接続元ノード */
	private NodeModel source;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	
	/** 接続先ノード */
	private NodeModel target;
	
	/** 作成するコネクションモデル */
	private ConnectionModel connection;
	
	/** Figureサイズ */
	@SuppressWarnings("unused")
	private Dimension figureSize;
	
	private JiemamyViewFacade jiemamyFacade;
	
	private SavePoint save;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param connection 作成するコネクションモデル
	 */
	public CreateConnectionCommand(JiemamyContext rootModel, int diagramIndex, ConnectionModel connection) {
		this.diagramIndex = diagramIndex;
		this.connection = connection;
		
		jiemamyFacade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
		logger.debug(LogMarker.LIFECYCLE, "construct");
	}
	
	@Override
	public boolean canExecute() {
		logger.debug(LogMarker.LIFECYCLE, "canExecute");
		
		if (source == null || target == null) {
			logger.debug("source or target is null: " + source + " " + target);
			return false;
		}
		
		// Viewとはコネクションが貼れない
		if (source.unwrap() instanceof ViewModel || target.unwrap() instanceof ViewModel) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_01);
			return false;
		}
		
		// 現状、付箋とはコネクションが貼れない
		if (source.unwrap() instanceof StickyModel || target.unwrap() instanceof StickyModel) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_02);
			return false;
		}
		
		// カラムが1つもないテーブルからは外部キーが貼れない
		if (((TableModel) source.unwrap()).getColumns().size() < 1) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_03);
			return false;
		}
		
		// ローカルキーが1つもないテーブルへは外部キーが貼れない
		if (getKey((TableModel) target.unwrap()) == null) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_04);
			return false;
		}
		
		// THINK 違うキー同士で参照してる可能性は？
//		if (connection.unwrap() != null) {
//			// 循環参照の禁止（ターゲットの親に自分がいたら、参照不可）
//			
//			Collection<DatabaseObjectModel> refs = EntityUtil.getReferenceEntities(target.unwrap(), true);
//			if (refs.contains(source.unwrap())) {
//				LogUtil.log(JiemamyPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_05);
//				return false;
//			}
//		}
		
		return true;
	}
	
	@Override
	public void execute() {
		logger.debug(LogMarker.LIFECYCLE, "execute");
		if (connection.unwrap() != null) {
			TableModel sourceTable = (TableModel) source.unwrap();
			TableModel targetTable = (TableModel) target.unwrap();
			save = jiemamyFacade.save();
			jiemamyFacade.createConnection(diagramIndex, connection, sourceTable, targetTable);
		} else {
			// TODO wrapping connectionしか対応していない。
		}
		
		jiemamyFacade.resetBendpoint(diagramIndex, connection);
	}
	
	/**
	 * Figureサイズを設定する。
	 * 
	 * @param figureSize Figureサイズ
	 */
	public void setFigureSize(Dimension figureSize) {
		this.figureSize = figureSize;
	}
	
	/**
	 * 接続元ノードを設定する。
	 * 
	 * @param source 接続元ノード
	 */
	public void setSource(NodeModel source) {
		logger.debug(LogMarker.LIFECYCLE, "setSource");
		logger.debug(LogMarker.DETAIL, "source = " + source);
		this.source = source;
		//		connection.setSource(source);
	}
	
	/**
	 * 接続先ノードを設定する。
	 * 
	 * @param target 接続先ノード
	 */
	public void setTarget(NodeModel target) {
		logger.debug(LogMarker.LIFECYCLE, "setTarget");
		logger.debug(LogMarker.DETAIL, "target = " + target);
		this.target = target;
		//		connection.setTarget(target);
	}
	
	@Override
	public void undo() {
		logger.debug(LogMarker.LIFECYCLE, "undo");
		if (connection.unwrap() != null) {
			jiemamyFacade.rollback(save);
		} else {
			// TODO wrapping connectionしか対応していない。
		}
	}
	
	/**
	 * 主キーがあれば主キー、なければ何らかのLocalKeyConstraintを取得する。
	 * 
	 * @param tableModel 検索するテーブル
	 * @return キー. 見つからなかった場合は{@code null}
	 */
	private LocalKeyConstraint getKey(TableModel tableModel) {
		LocalKeyConstraint key = null;
		try {
			key = tableModel.getPrimaryKey();
		} catch (ElementNotFoundException e) {
			List<LocalKeyConstraint> attributes = tableModel.findAttributes(LocalKeyConstraint.class);
			if (attributes.size() > 0) {
				key = attributes.get(0);
			}
		}
		return key;
	}
}
