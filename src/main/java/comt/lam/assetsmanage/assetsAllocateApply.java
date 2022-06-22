package comt.lam.assetsmanage;

import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;

import java.util.EventObject;
import java.util.HashMap;

public class assetsAllocateApply extends dataProcess implements RowClickEventListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        EntryGrid entryGrid = this.getView().getControl("comt_assetsallocate_en");
        entryGrid.addRowClickListener(this);
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
            this.getView().showErrorNotification("请选择一行");
            return;
        }
        Object materiel = rowsData[selectRows[0]].get("comt_materiel");
        int index = this.getModel().createNewEntryRow("comt_arrange_entry");
        this.getModel().setValue("comt_name", materiel, index);
    }

    private void quickApply() {
        HashMap<String, Object> map = new HashMap<>();
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("comt_assetsallocate_en");
        map.put("entry", entryEntity);
        FormShowParameter parameter = new FormShowParameter();
        parameter.setFormId("comt_quickallocateplace");
        parameter.setCaption("快速分配资产场地");
        parameter.getOpenStyle().setShowType(ShowType.Modal);
        parameter.setCloseCallBack(new CloseCallBack(this, "comt_quickallocateplace"));
        parameter.setShowClose(true);
        parameter.setCustomParams(map);
        this.getView().showForm(parameter);
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if ("comt_quickallocateplace".equals(closedCallBackEvent.getActionId())) {
            Object returnData = closedCallBackEvent.getReturnData();
            this.getView().showTipNotification(String.valueOf(returnData));
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        this.getView().updateView();
    }
}
