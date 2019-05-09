package fi.nls.oskari.control.myplaces;

import java.util.Arrays;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.service.user.UserLayerService;

@Oskari
public class MyPlacesWFSHelper extends UserLayerService {

    public static final String PROP_MYPLACES_BASELAYER_ID = "myplaces.baselayer.id";

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final String MYPLACES_ATTR_GEOMETRY = "oskari:geometry";
    private static final String MYPLACES_ATTR_CATEGORY_ID = "oskari:category_id";

    private FilterFactory ff;
    private int myPlacesLayerId;
    private MyPlacesService service;

    public MyPlacesWFSHelper() {
        init();
    }

    public void init() {
        this.ff = CommonFactoryFinder.getFilterFactory();
        this.myPlacesLayerId = PropertyUtil.getOptional(PROP_MYPLACES_BASELAYER_ID, -2);
    }

    public void getLayers(User user) throws ServiceException {
        throw new ServiceException("Not implemented");
    }
    public void getLayer(String layerId, User user) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    public int getBaselayerId() {
        return myPlacesLayerId;
    }

    public boolean isMyPlacesLayer(OskariLayer layer) {
        return layer.getId() == myPlacesLayerId;
    }

    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(PREFIX_MYPLACES);
    }

    public int parseId(String layerId) {
        return Integer.parseInt(layerId.substring(PREFIX_MYPLACES.length()));
    }

    public Filter getWFSFilter(String layerId, ReferencedEnvelope bbox) {
        int categoryId = parseId(layerId);
        Expression _categoryId = ff.property(MYPLACES_ATTR_CATEGORY_ID);

        Filter categoryIdEquals = ff.equals(_categoryId, ff.literal(categoryId));
        Filter bboxFilter = ff.bbox(MYPLACES_ATTR_GEOMETRY,
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                CRS.toSRS(bbox.getCoordinateReferenceSystem()));

        return ff.and(Arrays.asList(categoryIdEquals, bboxFilter));
    }

    public boolean hasViewPermission(String id, User user) {
        MyPlaceCategory layer = getLayer(parseId(id));
        if (layer == null) {
            return false;
        }
        return layer.isOwnedBy(user.getUuid()) || layer.isPublished();
    }

    private MyPlaceCategory getLayer(int id) {
        if (service == null) {
            // might cause problems with timing of components being initialized if done in init/constructor
            service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        }
        return service.findCategory(id);
    }
}