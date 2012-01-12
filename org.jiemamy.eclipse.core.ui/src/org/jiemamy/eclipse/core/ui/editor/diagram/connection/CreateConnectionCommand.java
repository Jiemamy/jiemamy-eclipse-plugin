/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.connection;

import java.util.Collection;

import com.google.common.collect.Iterables;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;
import org.seasar.eclipse.common.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.model.DbObject;
import org.jiemamy.model.JmNode;
import org.jiemamy.model.JmStickyNode;
import org.jiemamy.model.SimpleDbObjectNode;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.constraint.JmLocalKeyConstraint;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.table.SimpleJmTable;
import org.jiemamy.model.view.JmView;
import org.jiemamy.utils.LogMarker;

/**
 * コネクション作成GEFコマンド。
 * 
 * @author daisuke
 */
public class CreateConnectionCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(CreateConnectionCommand.class);
	
	/** 接続元ノード */
	private JmNode source;
	
	/** 接続先ノード */
	private JmNode target;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	
	/** Figureサイズ */
	@SuppressWarnings("unused")
	private Dimension figureSize;
	
	private final JiemamyContext context;
	
	private DbObject sourceCore;
	
	private DbObject targetCore;
	
	private final ForeignKeyCreation creation;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param context ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param creation 作成するコネクションモデル
	 */
	public CreateConnectionCommand(JiemamyContext context, int diagramIndex, ForeignKeyCreation creation) {
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.creation = creation;
		
		logger.trace(LogMarker.LIFECYCLE, "construct");
	}
	
	@Override
	public boolean canExecute() {
		logger.trace(LogMarker.LIFECYCLE, "canExecute");
		
		if (source == null || target == null) {
			logger.trace("source or target is null: " + source + " " + target);
			return false;
		}
		
		// Viewとはコネクションが貼れない
		if (sourceCore instanceof JmView || targetCore instanceof JmView) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_01);
			return false;
		}
		
		// 現状、付箋とはコネクションが貼れない
		if (source instanceof JmStickyNode || target instanceof JmStickyNode) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_02);
			return false;
		}
		
		// カラムが1つもないテーブルからは外部キーが貼れない
		if (((JmTable) sourceCore).getColumns().size() < 1) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_03);
			return false;
		}
		
		// ローカルキーが1つもないテーブルへは外部キーが貼れない
		if (getKey((JmTable) targetCore) == null) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_04);
			return false;
		}
		
		// 循環参照の禁止（ターゲットの親に自分がいたら、参照不可）
		// THINK 違うキー同士で参照してる可能性は？
		// ECL-90
		Collection<DbObject> superDbObjectsRecursive = context.findSuperDbObjectsRecursive(targetCore);
		if (superDbObjectsRecursive.contains(sourceCore)) {
			LogUtil.log(JiemamyUIPlugin.getDefault(), Messages.CreateConnectionCommand_log_canExecute_05);
			return false;
		}
		
		return true;
	}
	
	@Override
	public void execute() {
		logger.debug(LogMarker.LIFECYCLE, "execute");
		SimpleJmDiagram diagram =
				(SimpleJmDiagram) context.getFacet(DiagramFacet.class).getDiagrams().get(diagramIndex);
		creation.setSourceTable((SimpleJmTable) sourceCore);
		creation.setTargetTable((SimpleJmTable) targetCore);
		creation.execute(context, diagram);
		
//		jiemamyFacade.resetBendpoint(diagramIndex, connection);
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
	public void setSource(JmNode source) {
		logger.trace(LogMarker.LIFECYCLE, "setSource");
		logger.trace(LogMarker.DETAIL, "source = " + source);
		this.source = source;
		if (source instanceof SimpleDbObjectNode) {
			SimpleDbObjectNode dboJmNode = (SimpleDbObjectNode) source;
			sourceCore = context.resolve(dboJmNode.getCoreModelRef());
		}
		creation.setSource(source.toReference());
	}
	
	/**
	 * 接続先ノードを設定する。
	 * 
	 * @param target 接続先ノード
	 */
	public void setTarget(JmNode target) {
		logger.trace(LogMarker.LIFECYCLE, "setTarget");
		logger.trace(LogMarker.DETAIL, "target = " + target);
		this.target = target;
		if (target instanceof SimpleDbObjectNode) {
			SimpleDbObjectNode dboJmNode = (SimpleDbObjectNode) target;
			targetCore = context.resolve(dboJmNode.getCoreModelRef());
		}
		creation.setTarget(target.toReference());
	}
	
	@Override
	public void undo() {
		logger.debug(LogMarker.LIFECYCLE, "undo");
		SimpleJmDiagram diagram =
				(SimpleJmDiagram) context.getFacet(DiagramFacet.class).getDiagrams().get(diagramIndex);
		creation.undo(context, diagram);
	}
	
	/**
	 * 主キーがあれば主キー、なければ何らかのJmLocalKeyConstraintを取得する。
	 * 
	 * @param table 検索するテーブル
	 * @return キー. 見つからなかった場合は{@code null}
	 */
	private JmLocalKeyConstraint getKey(JmTable table) {
		JmLocalKeyConstraint key = table.getPrimaryKey();
		if (key == null) {
			Collection<JmLocalKeyConstraint> localKeys = table.getConstraints(JmLocalKeyConstraint.class);
			if (localKeys.size() > 0) {
				key = Iterables.get(localKeys, 0);
			}
		}
		return key;
	}
}
