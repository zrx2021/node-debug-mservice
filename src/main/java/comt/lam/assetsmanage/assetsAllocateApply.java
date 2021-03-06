package comt.lam.assetsmanage;

import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.AfterDeleteRowEventArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.AfterF7SelectEvent;
import kd.bos.form.field.events.AfterF7SelectListener;
import kd.bos.form.operate.FormOperate;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Set;

public class assetsAllocateApply extends dataProcess implements RowClickEventListener, AfterF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        EntryGrid entryGrid = this.getView().getControl("comt_assetsallocate_en");
        BasedataEdit basedataEdit = this.getView().getControl("comt_place");
        entryGrid.addRowClickListener(this);
        this.addItemClickListeners("bar_save", "bar_submitandnew", "bar_submit", "bar_audit");
        basedataEdit.addAfterF7SelectListener(this);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        if ("quickapply".equals(formOperate.getOperateKey())) {
            quickApply();
            return;
        } else if (!formOperate.getOperateKey().equals("newentry")) return;
        args.setCancel(true);
        EntryGrid selectionEntry = this.getView().getControl("comt_assetsallocate_en");
        DynamicObject[] rowsData = selectionEntry.getEntryData().getDataEntitys();
        int[] selectRows = selectionEntry.getSelectRows();
        if (selectRows.length != 1) {
            this.getView().showErrorNotification("???????????????");
            return;
        }
        Object materiel = rowsData[selectRows[0]].get("comt_materiel");
        int index = this.getModel().createNewEntryRow("comt_arrange_entry");
        this.getModel().setValue("comt_name", materiel, index);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        if ("bar_save".equals(evt.getItemKey()) || "bar_submitandnew".equals(evt.getItemKey()) || "bar_submit".equals(evt.getItemKey()) || "bar_audit".equals(evt.getItemKey())) {
            updateApplyCount();
            String hasEmptyMessage = this.CheckEntry("comt_assetsallocate_en");
            if (hasEmptyMessage == null) return;
            this.getView().showErrorNotification("?????????????????????????????????????????????????????????");
            evt.setCancel(true);
        }
    }

    private void quickApply() {
        HashMap<String, Object> map = new HashMap<>();
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("comt_assetsallocate_en");
        map.put("entry", entryEntity);
        FormShowParameter parameter = new FormShowParameter();
        parameter.setFormId("comt_quickallocateplace");
        parameter.setCaption("????????????????????????");
        parameter.getOpenStyle().setShowType(ShowType.Modal);
        parameter.setCloseCallBack(new CloseCallBack(this, "quickallocateplacecallback"));
        parameter.setShowClose(true);
        parameter.setCustomParams(map);
        this.getView().showForm(parameter);
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (!"quickallocateplacecallback".equals(closedCallBackEvent.getActionId())) return;
        DynamicObjectCollection returnData = (DynamicObjectCollection) closedCallBackEvent.getReturnData();
        if (returnData == null) return;
        IDataModel model = this.getModel();
        model.deleteEntryData("comt_arrange_entry");

        for (DynamicObject parentsRow : returnData) {
            DynamicObjectCollection subEntry = parentsRow.getDynamicObjectCollection("comt_subentry");
            Object materiel = parentsRow.get("comt_materiel");
            for (DynamicObject subRow : subEntry) {
                int newRowsCount = subRow.getBigDecimal("comt_stepper").intValue();
                Object place = subRow.get("comt_place");
                for (int i = 0; i < newRowsCount; i++) {
                    int newRowIndex = model.createNewEntryRow("comt_arrange_entry");
                    DynamicObject newRow = model.getEntryRowEntity("comt_arrange_entry", newRowIndex);
                    newRow.set("comt_name", materiel);
                    newRow.set("comt_place", place);
                }
            }
        }
        updateApplyCount();
        this.getView().updateView();
    }

    private void updateApplyCount() {
        IDataModel model = this.getModel();
        DynamicObjectCollection arrangeEntry = model.getEntryEntity("comt_arrange_entry");
        DynamicObjectCollection selectEntry = model.getEntryEntity("comt_assetsallocate_en");
        HashMap<Object, Integer> applyCounts = new HashMap<>();
        HashMap<Object, Integer> mapSelectEntry = new HashMap<>();//??????????????????????????????????????????get?????????????????????

        for (DynamicObject arrangeRow : arrangeEntry) {
            DynamicObject name = (DynamicObject) arrangeRow.get("comt_name");
            if (name != null) {
                applyCounts.merge(name.getPkValue(), 1, Integer::sum);
            }
        }

        for (DynamicObject selectionRow : selectEntry) {
            DynamicObject materiel = (DynamicObject) selectionRow.get("comt_materiel");
            Object pkValue = materiel.getPkValue();
            Integer index = mapSelectEntry.get(pkValue);
            if (index == null) {
                mapSelectEntry.put(pkValue, selectEntry.indexOf(selectionRow));
            } else {
                this.getView().showErrorNotification("??????????????????????????????????????????????????????????????????");
            }
        }

        Set<Object> nameSet = applyCounts.keySet();

        for (Object name : nameSet) {
            Integer applyCount = applyCounts.get(name);
            Integer index = mapSelectEntry.get(name);
            if (index != null) {
                model.setValue("comt_applycount", applyCount, index);
            }
            if (index == null) {
                this.getView().showErrorNotification("????????????????????????????????????????????????????????????");
                //TODO ???????????????????????????????????????????????????????????????????????????????????????????????????
            }
        }
        this.getView().updateView();
    }

    @Override
    public void afterDeleteRow(AfterDeleteRowEventArgs e) {
        super.afterDeleteRow(e);
        updateApplyCount();
    }

    @Override
    public void afterF7Select(AfterF7SelectEvent afterF7SelectEvent) {
        updateApplyCount();
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        this.getView().updateView();
    }
}
