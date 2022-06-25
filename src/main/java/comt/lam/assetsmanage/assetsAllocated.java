package comt.lam.assetsmanage;

import comt.lam.dataProcess;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.IListModel;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.list.BillList;

import java.util.EventObject;

public class assetsAllocated extends dataProcess {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        IDataModel model = this.getModel();
        Object status = model.getValue("status");
        if (!"C".equals(status.toString())) return;
        Object assetsNo = model.getValue("number");

        BillList billList = getView().getControl("comt_billlistap");
        IListModel listModel = billList.getListModel();
//        listModel.getDataCount();
//        int count = 0;
//        for (DynamicObject row : entryEntity) {
//            Object number = row.get("comt_bookshelf.number");
//            if (assetsNo.equals(number)) {
//                count++;
//            }
//        }
//        model.setValue("comt_cellcounttotal", count);
//        this.getView().updateView();
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);


    }

    private void CellCounter() {

    }
}
