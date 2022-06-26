package comt.lam.basedatamanage;

import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.GetEntityTypeEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.EventObject;

public class bookBaseData extends dataProcess {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        IDataModel model = this.getModel();
        String number = model.getValue("number").toString();
        QFilter[] qFilter = new QFilter[2];
        qFilter[0] = new QFilter("comt_book_subentry.comt_subbookname.number", QCP.equals, number);
        qFilter[1] = new QFilter("comt_book_subentry.comt_bookstatus", QCP.equals, "A");
        DynamicObject[] comt_books = BusinessDataServiceHelper.load("comt_book", "comt_bookno,comt_subbookname,comt_detail,comt_bookstatus", qFilter);

        model.deleteEntryData("comt_addnewbook_entry");

        int canUse = 0;
        for (DynamicObject comt_book : comt_books) {
            DynamicObjectCollection subEntry = (DynamicObjectCollection) comt_book.get("comt_book_subentry");
            for (DynamicObject subEntryRow : subEntry) {
                if (!number.equals(subEntryRow.get("comt_subbookname.number"))) continue;
                int index = model.createNewEntryRow("comt_addnewbook_entry");
                Object bookNumber = subEntryRow.get("comt_bookno");
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
        if ("order".equals(source.getOperateKey()) || "lent".equals(source.getOperateKey())) {
            //TODO 预约与借阅
        }
    }
}
