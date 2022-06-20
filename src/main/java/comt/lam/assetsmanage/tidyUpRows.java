package comt.lam.assetsmanage;

import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.datamodel.IEntryOperate;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.property.BasedataProp;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.RowClickEvent;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.AfterF7SelectEvent;
import kd.bos.form.field.events.AfterF7SelectListener;
import kd.bos.form.operate.FormOperate;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Set;

public class tidyUpRows extends dataProcess implements AfterF7SelectListener, RowClickEventListener {
    @Override
    public void registerListener(EventObject e) {
        BasedataEdit base = this.getView().getControl("comt_places");
        super.registerListener(e);
        base.addAfterF7SelectListener(this);
        this.addItemClickListeners();
        EntryGrid entryGrid = this.getView().getControl("comt_assetaclloapp_en");
        entryGrid.addRowClickListener(this);
    }

    @Override
    public void entryRowClick(RowClickEvent evt) {
        RowClickEventListener.super.entryRowClick(evt);
        tidyUp();
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        IEntryOperate entryOperate = this.getModel();
        EntryGrid entryGrid = this.getView().getControl("comt_subentry");
        if (args.getSource() instanceof FormOperate) {
            FormOperate formOperate = (FormOperate) args.getSource();
            String operateKey = formOperate.getOperateKey();
            if (operateKey.equals("deletesubentry")) {
                int rowCount = entryOperate.getEntryRowCount("comt_subentry");
                int length = entryGrid.getSelectRows().length;
                if (rowCount == 1 || length == rowCount) {
                    args.setCancel(true);
                    this.getView().showErrorNotification("无法删除最后一行！");
                }
            }
        }
    }

    public void beforeItemClick(BeforeItemClickEvent e) {
        if (StringUtils.equals("bar_save", e.getItemKey()) || StringUtils.equals("bar_modify", e.getItemKey()) || StringUtils.equals("bar_submitandnew", e.getItemKey()) || StringUtils.equals("bar_submit", e.getItemKey()) || StringUtils.equals("bar_audit", e.getItemKey())) {
            tidyUp();
            if (this.CheckFailure("comt_assetaclloapp_en", "comt_subentry")) {
                e.setCancel(true);
            }
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        if (StringUtils.equals("comt_stepper", e.getProperty().getName())) {
            tidyUp();
        }
    }

    @Override
    public void afterF7Select(AfterF7SelectEvent afterF7SelectEvent) {
        tidyUp();
    }

    private void tidyUp() {
        MainEntityType mainEntityType = this.getModel().getDataEntityType();
        IEntryOperate entryOperate = this.getModel();
        BasedataProp basedataProp = (BasedataProp) mainEntityType.findProperty("comt_places");
        DynamicObjectCollection Rows = entryOperate.getEntryEntity("comt_subentry");
        HashMap<DynamicObject, Integer> baseDateMap = new HashMap<>();
        DynamicObject rowBaseData = null;
        int total = 0;
        if (Rows == null)
            return;
        for (DynamicObject row : Rows) {
            int stepperValue;
            rowBaseData = (DynamicObject) basedataProp.getValue(row);
            stepperValue = row.getInt("comt_stepper");
            if (rowBaseData == null)//如果基础资料为空则继续读取下一行，不会计入
                continue;
            Integer count = baseDateMap.get(rowBaseData);
            if (count == null) {
                count = stepperValue;
            } else {
                count += stepperValue;
            }
            baseDateMap.put(rowBaseData, count);
        }
        boolean baseIsNotNull = (rowBaseData != null);//最后一行的基础资料不为空
        boolean moreThanOneRows = (entryOperate.getEntryRowCount("comt_subentry") > 1);//单据体行超过1行
        if ((baseIsNotNull && moreThanOneRows) || (baseIsNotNull ^ moreThanOneRows))
            entryOperate.deleteEntryData("comt_subentry");
        Set<DynamicObject> BaseDataSet = baseDateMap.keySet();
        int i = 0;
        for (DynamicObject baseData : BaseDataSet) {
            int count = baseDateMap.get(baseData);
            entryOperate.createNewEntryRow("comt_subentry");
            DynamicObject row = entryOperate.getEntryRowEntity("comt_subentry", i);
            row.set("comt_places", baseData);
            row.set("comt_stepper", count);
            total += count;
            i++;
        }
        this.getModel().setValue("comt_applycount", total, entryOperate.getEntryCurrentRowIndex("comt_assetaclloapp_en"));
        this.getView().updateView("comt_subentry");
    }
}