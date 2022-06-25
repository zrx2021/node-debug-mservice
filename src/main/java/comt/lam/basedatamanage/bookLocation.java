package comt.lam.basedatamanage;

import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleDynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;

import java.util.EventObject;

public class bookLocation extends dataProcess {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        if (!"已审核".equals(this.getModel().getValue("status"))) return;
        IDataModel model = this.getModel();
        DynamicObjectCollection entry = model.getEntryEntity("comt_listgridviewap");
        int onShelf = 0, stored = 0;

        for (int i = 0; i < entry.size(); i++) {
            Object bookStatus = model.getValue("comt_bookstatus");
            if ("A".equals(bookStatus)) onShelf++;
            stored++;
        }
        model.setValue("comt_count", onShelf);
        model.setValue("comt_stored", stored);
        this.getView().updateView();
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        if ("comt_bookshelf".equals(e.getProperty().getName())) {
            IDataModel model = this.getModel();
            DynamicObject bookShelf = (DynamicObject) model.getValue("comt_bookshelf");
            if (bookShelf == null) return;
            Object capacity = bookShelf.get("comt_capacity");
            model.setValue("comt_size", capacity);
            DynamicObject basePlace = (DynamicObject) bookShelf.get("comt_baseplace");
            LocaleDynamicObjectCollection text = (LocaleDynamicObjectCollection) bookShelf.getDataEntityType().getProperties().get("multilanguagetext").getValue(basePlace);
            String location = text.get(0).getString(3).replace(".", "");//获得书架所在场地的长名称，并去除掉“.”
            model.setValue("comt_bookshelfplace", location);
            String number = (String) bookShelf.get("number");//获取书架的编号
            String[] split = number.split("-");//获取书架的编号后，以符号“-”分割成两个字符串
            //TODO 此图书馆藏位置需要修改在资产基础资料里面的书架里面的格子已分配量，这个分配量有待加入，然后根据已分配的量去赋值序号
            int order = 1;//序号，第n个分配的格子，需从是书架的资产基础资料获取“已分配单元格量”加1后赋值
            model.setValue("number", "cell-" + split[1] + "-" + String.format("%03d", order));//格子编码设置，格式为：书架的编号-格子序号（书架编号未去除前面多余的0）
            int intNumber = Integer.parseInt(split[1]);//去除多余的0
            int columnCount = (int) bookShelf.get("comt_columncount");//获取是书架的资产基础资料的行数和列数
            int inRow = (int) Math.ceil(order * 1.0 / columnCount);//由左到右由上到下，根据单元格序号、书架行数、书架列数获取单元格所在行、所在列
            int inColumn = order % columnCount == 0 ? columnCount : order % columnCount;
            model.setValue("name", String.format("%03d", intNumber) + "架" + inRow + "行" + inColumn + "列");//单元格名字
        }

    }
}
