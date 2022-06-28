package comt.lam.basedatamanage;

import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.GetEntityTypeEventArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.EventObject;
import java.util.HashMap;

public class bookBaseData extends dataProcess {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        IDataModel model = this.getModel();
        if ((model.getEntryEntity("comt_addnewbook_entry")).size() != 0) return;
        String number = model.getValue("number").toString();
        QFilter[] qFilter = new QFilter[2];
        qFilter[0] = new QFilter("comt_book_subentry.comt_subbookname.number", QCP.equals, number);
        qFilter[1] = new QFilter("comt_book_subentry.comt_bookstatus", QCP.equals, "A");
        DynamicObject[] comt_books = BusinessDataServiceHelper.load("comt_book", "comt_bookno,comt_subbookname,comt_detail,comt_bookstatus", qFilter);

        model.deleteEntryData("comt_addnewbook_entry");
        int canUse = 0;
        HashMap<Object, Integer> numFlag = new HashMap<>();
        for (DynamicObject comt_book : comt_books) {
            DynamicObjectCollection subEntry = (DynamicObjectCollection) comt_book.get("comt_book_subentry");
            for (DynamicObject subEntryRow : subEntry) {
                if (!number.equals(subEntryRow.get("comt_subbookname.number"))) continue;
                Object bookNumber = subEntryRow.get("comt_bookno");
                if (numFlag.get(bookNumber) == null) numFlag.put(bookNumber, 1);
                else if (numFlag.get(bookNumber) == 1) continue;
                int index = model.createNewEntryRow("comt_addnewbook_entry");
                model.setValue("comt_number", bookNumber, index);
                Object bookName = ((DynamicObject) subEntryRow.get("comt_subbookname")).get("name");
                model.setValue("comt_name", bookName.toString(), index);
                Object detail = subEntryRow.get("comt_detail");
                model.setValue("comt_detail", detail, index);
                String bookStatus = (String) subEntryRow.get("comt_bookstatus");
                if ("A".equals(bookStatus)) {
                    bookStatus = "可借阅";
                    canUse++;
                }
                model.setValue("comt_bookstatus1", bookStatus, index);
            }
        }
        model.setValue("comt_canuse", canUse);
        this.getView().updateView();
    }

    @Override
    public void getEntityType(GetEntityTypeEventArgs e) {
        super.getEntityType(e);

    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate source = (FormOperate) args.getSource();
        String operateKey = source.getOperateKey();
        if ("order".equals(operateKey) || "lent".equals(operateKey)) {
            //TODO 预约与借阅施工中
            EntryGrid entry = this.getView().getControl("comt_addnewbook_entry");
            IDataModel model = this.getModel();
            int[] selectRows = entry.getSelectRows();
            DynamicObject selectRow = model.getEntryRowEntity("comt_addnewbook_entry", selectRows[0]);

            if (selectRows.length != 1) {
                this.getView().showErrorNotification("错误，需要选一行");
                return;
            }
            if (!"可借阅".equals(model.getValue("comt_bookstatus1", selectRows[0]).toString())) {
                this.getView().showErrorNotification("错误，只能选择可借阅的图书");
                return;
            }


            Object pkValue = model.getDataEntity().getPkValue();
            HashMap<String, Object> map = new HashMap<>();
            map.put("lent", selectRow);
            map.put("pkid", pkValue);
            map.put("operate", operateKey);
            FormShowParameter parameter = new FormShowParameter();
            parameter.setFormId("comt_lent");
            parameter.setCaption("我的借阅申请单");
            parameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            parameter.setCloseCallBack(new CloseCallBack(this, "lentbookcallback"));
            parameter.setShowClose(true);
            parameter.setCustomParams(map);
            this.getView().showForm(parameter);
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);

    }
}
