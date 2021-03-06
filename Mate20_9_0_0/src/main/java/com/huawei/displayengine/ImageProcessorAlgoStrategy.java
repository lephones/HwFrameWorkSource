package com.huawei.displayengine;

import android.os.SystemProperties;
import android.os.Trace;
import com.huawei.displayengine.ImageProcessor.AlgoType;
import com.huawei.displayengine.ImageProcessor.ColorspaceType;
import com.huawei.displayengine.ImageProcessor.ImageType;
import com.huawei.displayengine.ImageProcessor.ThumbnailType;
import com.huawei.displayengine.ImageProcessor.TileEngineType;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class ImageProcessorAlgoStrategy {
    private static final String TAG = "DE J ImageProcessorAlgoStrategy";
    private static final String XML_PATH = "display/effect/algorithm/imageprocessor/ImageProcessStrategy.xml";
    private static boolean mAlgoSkinBeautyEnable;
    private static boolean mAlgoVividEnable;
    private static boolean mAlgoWideColorSpaceEnable;
    private static ThumbnailType mCurrentThumbnailType;
    private static TileEngineType mCurrentTileType;
    private static boolean mEnable;
    private static boolean mNeedNormallizeColorSpace;
    private static ColorspaceType mNormallizeColorGamut = ColorspaceType.SRGB;
    private static Map<ThumbnailType, ThumbnailStrategy> mThumbnailStrategy = new HashMap();
    private static Map<TileEngineType, TileStrategy> mTileStrategy = new HashMap();

    private static class ThumbnailStrategy {
        public Map<ImageType, Set<AlgoType>> mAlgos;
        public Map<ImageType, Set<AlgoType>> mCommonInfoAlgos;
        public boolean mIsSaveCommonInfo;

        public ThumbnailStrategy(ImageType imageType, Set<AlgoType> algos) {
            this(imageType, algos, false);
        }

        public ThumbnailStrategy(ImageType imageType, Set<AlgoType> algos, boolean isSaveCommonInfo) {
            this.mIsSaveCommonInfo = isSaveCommonInfo;
            if (imageType != null && algos != null) {
                this.mAlgos = new HashMap();
                this.mAlgos.put(imageType, algos);
                if (isSaveCommonInfo) {
                    this.mCommonInfoAlgos = new HashMap();
                    this.mCommonInfoAlgos.put(imageType, EnumSet.copyOf(algos));
                }
            }
        }
    }

    public static class TileStrategy {
        public Map<ImageType, Set<AlgoType>> mAlgos;

        public TileStrategy(ImageType imageType, Set<AlgoType> algos) {
            if (imageType != null && algos != null) {
                this.mAlgos = new HashMap();
                this.mAlgos.put(imageType, algos);
            }
        }
    }

    private static class Element_Config extends HwXmlElement {
        private Element_Config() {
        }

        public String getName() {
            return "Config";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_Custom extends HwXmlElement {
        private Element_Custom() {
        }

        public String getName() {
            return "Custom";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_CustomGroup extends HwXmlElement {
        private Element_CustomGroup() {
        }

        public String getName() {
            return "CustomGroup";
        }

        protected List<String> getNameList() {
            return Arrays.asList(new String[]{"AlgoWideColorSpace", "AlgoSkinBeauty", "AlgoVivid", "NormallizeColorGamut"});
        }

        protected boolean isOptional() {
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0091  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0085  */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0079  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0062  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0091  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0085  */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0079  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0062  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0091  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0085  */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0079  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0062  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0091  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0085  */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0079  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0062  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            boolean z;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -2091365941) {
                if (hashCode != -1723209940) {
                    if (hashCode != -1052144663) {
                        if (hashCode == 1255667550 && valueName.equals("NormallizeColorGamut")) {
                            z = true;
                            String gamutName;
                            switch (z) {
                                case false:
                                    ImageProcessorAlgoStrategy.mAlgoWideColorSpaceEnable = HwXmlElement.string2Boolean(parser.nextText());
                                    break;
                                case true:
                                    ImageProcessorAlgoStrategy.mAlgoSkinBeautyEnable = HwXmlElement.string2Boolean(parser.nextText());
                                    break;
                                case true:
                                    ImageProcessorAlgoStrategy.mAlgoVividEnable = HwXmlElement.string2Boolean(parser.nextText());
                                    break;
                                case true:
                                    gamutName = parser.nextText();
                                    if (gamutName != null && gamutName.equals("Super Gamut")) {
                                        ImageProcessorAlgoStrategy.mNormallizeColorGamut = ColorspaceType.SUPER_GAMUT;
                                    }
                                    ImageProcessorAlgoStrategy.mNeedNormallizeColorSpace = true;
                                    break;
                                default:
                                    gamutName = this.TAG;
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("unknow valueName=");
                                    stringBuilder.append(valueName);
                                    DElog.e(gamutName, stringBuilder.toString());
                                    return false;
                            }
                            return true;
                        }
                    } else if (valueName.equals("AlgoWideColorSpace")) {
                        z = false;
                        switch (z) {
                            case false:
                                break;
                            case true:
                                break;
                            case true:
                                break;
                            case true:
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                } else if (valueName.equals("AlgoSkinBeauty")) {
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("AlgoVivid")) {
                z = true;
                switch (z) {
                    case false:
                        break;
                    case true:
                        break;
                    case true:
                        break;
                    case true:
                        break;
                    default:
                        break;
                }
                return true;
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
                case true:
                    break;
                case true:
                    break;
                default:
                    break;
            }
            return true;
        }

        protected boolean checkValue() {
            return ImageProcessorAlgoStrategy.mNormallizeColorGamut != null;
        }
    }

    private static class Element_Enable extends HwXmlElement {
        private Element_Enable() {
        }

        public String getName() {
            return "Enable";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            ImageProcessorAlgoStrategy.mEnable = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_Thumbnail extends HwXmlElement {
        private Element_Thumbnail() {
        }

        public String getName() {
            return "Thumbnail";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            ImageProcessorAlgoStrategy.mCurrentThumbnailType = ThumbnailType.valueOf(parser.getAttributeValue(null, "type"));
            return true;
        }
    }

    private static class Element_Thumbnail_Algo extends HwXmlElement {
        private Element_Thumbnail_Algo() {
        }

        public String getName() {
            return "Algo";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            String imageName = parser.getAttributeValue(null, "image");
            ImageType imageType = ImageType.NORMAL;
            if (imageName != null) {
                imageType = ImageType.valueOf(imageName);
            }
            if (!ImageProcessorAlgoStrategy.isImageTypeEnable(imageType)) {
                return true;
            }
            AlgoType algoType = AlgoType.valueOf(parser.nextText());
            ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) ImageProcessorAlgoStrategy.mThumbnailStrategy.get(ImageProcessorAlgoStrategy.mCurrentThumbnailType);
            if (thumbnailStrategy == null) {
                ImageProcessorAlgoStrategy.mThumbnailStrategy.put(ImageProcessorAlgoStrategy.mCurrentThumbnailType, new ThumbnailStrategy(imageType, EnumSet.of(algoType)));
            } else if (thumbnailStrategy.mAlgos == null) {
                thumbnailStrategy.mAlgos = new HashMap();
                thumbnailStrategy.mAlgos.put(imageType, EnumSet.of(algoType));
            } else {
                Set<AlgoType> set = (Set) thumbnailStrategy.mAlgos.get(imageType);
                if (set == null) {
                    thumbnailStrategy.mAlgos.put(imageType, EnumSet.of(algoType));
                } else {
                    set.add(algoType);
                }
            }
            return true;
        }
    }

    private static class Element_Thumbnail_SaveCommonInfo extends HwXmlElement {
        private Element_Thumbnail_SaveCommonInfo() {
        }

        public String getName() {
            return "SaveCommonInfo";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            boolean isSaveCommonInfo = HwXmlElement.string2Boolean(parser.nextText());
            if (!isSaveCommonInfo) {
                return true;
            }
            ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) ImageProcessorAlgoStrategy.mThumbnailStrategy.get(ImageProcessorAlgoStrategy.mCurrentThumbnailType);
            if (thumbnailStrategy == null) {
                ImageProcessorAlgoStrategy.mThumbnailStrategy.put(ImageProcessorAlgoStrategy.mCurrentThumbnailType, new ThumbnailStrategy(null, null, isSaveCommonInfo));
            } else {
                if (thumbnailStrategy.mAlgos != null) {
                    thumbnailStrategy.mCommonInfoAlgos = new HashMap();
                    for (Entry<ImageType, Set<AlgoType>> entry : thumbnailStrategy.mAlgos.entrySet()) {
                        thumbnailStrategy.mCommonInfoAlgos.put((ImageType) entry.getKey(), EnumSet.copyOf((Set) entry.getValue()));
                    }
                }
                thumbnailStrategy.mIsSaveCommonInfo = isSaveCommonInfo;
            }
            return true;
        }
    }

    private static class Element_Tile extends HwXmlElement {
        private Element_Tile() {
        }

        public String getName() {
            return "Tile";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            ImageProcessorAlgoStrategy.mCurrentTileType = TileEngineType.valueOf(parser.getAttributeValue(null, "type"));
            return true;
        }
    }

    private static class Element_Tile_Algo extends HwXmlElement {
        private Element_Tile_Algo() {
        }

        public String getName() {
            return "Algo";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            String imageName = parser.getAttributeValue(null, "image");
            ImageType imageType = ImageType.NORMAL;
            if (imageName != null) {
                imageType = ImageType.valueOf(imageName);
            }
            if (!ImageProcessorAlgoStrategy.isImageTypeEnable(imageType)) {
                return true;
            }
            AlgoType algoType = AlgoType.valueOf(parser.nextText());
            TileStrategy tileStrategy = (TileStrategy) ImageProcessorAlgoStrategy.mTileStrategy.get(ImageProcessorAlgoStrategy.mCurrentTileType);
            if (tileStrategy == null) {
                ImageProcessorAlgoStrategy.mTileStrategy.put(ImageProcessorAlgoStrategy.mCurrentTileType, new TileStrategy(imageType, EnumSet.of(algoType)));
            } else if (tileStrategy.mAlgos == null) {
                tileStrategy.mAlgos = new HashMap();
                tileStrategy.mAlgos.put(imageType, EnumSet.of(algoType));
            } else {
                Set<AlgoType> set = (Set) tileStrategy.mAlgos.get(imageType);
                if (set == null) {
                    tileStrategy.mAlgos.put(imageType, EnumSet.of(algoType));
                } else {
                    set.add(algoType);
                }
            }
            return true;
        }
    }

    public ImageProcessorAlgoStrategy() {
        DElog.i(TAG, "ImageProcessorAlgoStrategy enter");
        if (!isFactoryMode()) {
            loadXML();
            initCommonInfoAlgos();
        }
        printStrategy();
        DElog.i(TAG, "ImageProcessorAlgoStrategy exit");
    }

    private void printStrategy() {
        String str;
        StringBuilder stringBuilder;
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("printStrategy mEnable=");
        stringBuilder2.append(mEnable);
        stringBuilder2.append(", mAlgoWideColorSpaceEnable=");
        stringBuilder2.append(mAlgoWideColorSpaceEnable);
        stringBuilder2.append(", mAlgoSkinBeautyEnable=");
        stringBuilder2.append(mAlgoSkinBeautyEnable);
        stringBuilder2.append(", mAlgoVividEnable=");
        stringBuilder2.append(mAlgoVividEnable);
        stringBuilder2.append(", mNeedNormallizeColorSpace=");
        stringBuilder2.append(mNeedNormallizeColorSpace);
        stringBuilder2.append(", mNormallizeColorGamut=");
        stringBuilder2.append(mNormallizeColorGamut);
        DElog.i(str2, stringBuilder2.toString());
        for (Entry<ThumbnailType, ThumbnailStrategy> entry : mThumbnailStrategy.entrySet()) {
            String stringBuilder3;
            ThumbnailType thumbnailType = (ThumbnailType) entry.getKey();
            ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) entry.getValue();
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("printStrategy Thumbnail: ");
            stringBuilder.append(thumbnailType);
            stringBuilder.append(", Algo:");
            stringBuilder.append(thumbnailStrategy.mAlgos);
            if (thumbnailStrategy.mIsSaveCommonInfo) {
                StringBuilder stringBuilder4 = new StringBuilder();
                stringBuilder4.append(", CommonInfoAlgo:");
                stringBuilder4.append(thumbnailStrategy.mCommonInfoAlgos);
                stringBuilder3 = stringBuilder4.toString();
            } else {
                stringBuilder3 = "";
            }
            stringBuilder.append(stringBuilder3);
            DElog.i(str, stringBuilder.toString());
        }
        for (Entry<TileEngineType, TileStrategy> entry2 : mTileStrategy.entrySet()) {
            TileEngineType tileEngineType = (TileEngineType) entry2.getKey();
            TileStrategy tileStrategy = (TileStrategy) entry2.getValue();
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("printStrategy Tile: ");
            stringBuilder.append(tileEngineType);
            stringBuilder.append(", Algo:");
            stringBuilder.append(tileStrategy.mAlgos);
            DElog.i(str, stringBuilder.toString());
        }
    }

    private void initCommonInfoAlgos() {
        DElog.d(TAG, "initCommonInfoAlgos");
        for (ImageType imageType : ImageType.values()) {
            if (isImageTypeEnable(imageType)) {
                Set<AlgoType> set;
                Set<AlgoType> algos = EnumSet.noneOf(AlgoType.class);
                for (TileEngineType tileEngineType : mTileStrategy.keySet()) {
                    set = getTileAlgos(tileEngineType, imageType);
                    if (set != null) {
                        algos.addAll(set);
                    }
                }
                if (!algos.isEmpty()) {
                    for (ThumbnailStrategy thumbnailStrategy : mThumbnailStrategy.values()) {
                        if (thumbnailStrategy.mIsSaveCommonInfo) {
                            if (thumbnailStrategy.mCommonInfoAlgos == null) {
                                thumbnailStrategy.mCommonInfoAlgos = new HashMap();
                                thumbnailStrategy.mCommonInfoAlgos.put(imageType, algos);
                            } else {
                                set = (Set) thumbnailStrategy.mCommonInfoAlgos.get(imageType);
                                if (set == null) {
                                    thumbnailStrategy.mCommonInfoAlgos.put(imageType, algos);
                                } else {
                                    set.addAll(algos);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isImageTypeEnable(ImageType imageType) {
        switch (imageType) {
            case NORMAL:
                return true;
            case WIDE_COLOR_SPACE:
                return mAlgoWideColorSpaceEnable;
            case SKIN_BEAUTY:
                return mAlgoSkinBeautyEnable;
            case VIVID:
                return mAlgoVividEnable;
            default:
                return false;
        }
    }

    public boolean isImageProcessorEnable() {
        return mEnable;
    }

    public boolean isAlgoWideColorSpaceEnable() {
        return mAlgoWideColorSpaceEnable;
    }

    public boolean isAlgoSkinBeautyEnable() {
        return mAlgoSkinBeautyEnable;
    }

    public boolean isAlgoVividEnable() {
        return mAlgoVividEnable;
    }

    public boolean needNormallizeColorSpace() {
        return mNeedNormallizeColorSpace;
    }

    public ColorspaceType getNormallizeColorGamut() {
        return mNormallizeColorGamut;
    }

    public boolean needRunSoftwareAlgo(ThumbnailType thumbnailType, ImageType imageType) {
        if (getThumbnailAlgos(thumbnailType, imageType) == null && getCommonInfoAlgos(thumbnailType, imageType) == null) {
            return false;
        }
        return true;
    }

    public boolean needSaveCommonInfo(ThumbnailType thumbnailType) {
        ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) mThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null) {
            return false;
        }
        return thumbnailStrategy.mIsSaveCommonInfo;
    }

    public Set<AlgoType> getThumbnailAlgos(ThumbnailType thumbnailType, ImageType imageType) {
        ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) mThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null || thumbnailStrategy.mAlgos == null) {
            return null;
        }
        Set<AlgoType> set = (Set) thumbnailStrategy.mAlgos.get(imageType);
        if (set != null && !set.isEmpty()) {
            return set;
        }
        if (imageType != ImageType.NORMAL) {
            set = (Set) thumbnailStrategy.mAlgos.get(ImageType.NORMAL);
        }
        return set;
    }

    public Set<AlgoType> getCommonInfoAlgos(ThumbnailType thumbnailType, ImageType imageType) {
        ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) mThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null) {
            return null;
        }
        if (thumbnailStrategy.mCommonInfoAlgos == null) {
            return getThumbnailAlgos(thumbnailType, imageType);
        }
        Set<AlgoType> set = (Set) thumbnailStrategy.mCommonInfoAlgos.get(imageType);
        if (set != null && !set.isEmpty()) {
            return set;
        }
        if (imageType != ImageType.NORMAL) {
            set = (Set) thumbnailStrategy.mCommonInfoAlgos.get(ImageType.NORMAL);
        }
        return set;
    }

    public boolean needRunSoftwareAlgo(TileEngineType tileEngineType, ImageType imageType) {
        return getTileAlgos(tileEngineType, imageType) != null;
    }

    public Set<AlgoType> getTileEngineAlgos(TileEngineType tileEngineType) {
        TileStrategy tileStrategy = (TileStrategy) mTileStrategy.get(tileEngineType);
        if (tileStrategy == null || tileStrategy.mAlgos == null) {
            return null;
        }
        Set<AlgoType> set = null;
        for (Set<AlgoType> value : tileStrategy.mAlgos.values()) {
            if (set == null) {
                set = EnumSet.copyOf(value);
            } else {
                set.addAll(value);
            }
        }
        return set;
    }

    public Set<AlgoType> getTileAlgos(TileEngineType tileEngineType, ImageType imageType) {
        TileStrategy tileStrategy = (TileStrategy) mTileStrategy.get(tileEngineType);
        if (tileStrategy == null || tileStrategy.mAlgos == null) {
            return null;
        }
        Set<AlgoType> set = (Set) tileStrategy.mAlgos.get(imageType);
        if (set != null && !set.isEmpty()) {
            return set;
        }
        if (imageType != ImageType.NORMAL) {
            set = (Set) tileStrategy.mAlgos.get(ImageType.NORMAL);
        }
        return set;
    }

    private boolean isFactoryMode() {
        return "factory".equals(SystemProperties.get("ro.runmode", "factory"));
    }

    private void loadXML() {
        DElog.d(TAG, "loadXML()");
        Trace.traceBegin(8, "loadXML");
        try {
            if (!parseXml(getXmlPath())) {
                loadDefaultConfig();
            }
        } catch (RuntimeException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("loadXML RuntimeException ");
            stringBuilder.append(e);
            DElog.e(str, stringBuilder.toString());
            loadDefaultConfig();
        }
        Trace.traceEnd(8);
    }

    private String getXmlPath() {
        String xmlPath = null;
        File xmlFile = HwCfgFilePolicy.getCfgFile(XML_PATH, 0);
        if (xmlFile == null) {
            DElog.w(TAG, "getXmlPath() error! can't find xml file.display/effect/algorithm/imageprocessor/ImageProcessStrategy.xml");
            return null;
        }
        try {
            xmlPath = xmlFile.getCanonicalPath();
        } catch (IOException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getXmlPath() IOException ");
            stringBuilder.append(e);
            DElog.e(str, stringBuilder.toString());
        }
        return xmlPath;
    }

    private boolean parseXml(String xmlPath) {
        if (xmlPath == null) {
            DElog.w(TAG, "parseXml() error! xmlPath is null");
            return false;
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlPath);
        registerElement(xmlParser);
        if (!xmlParser.parse()) {
            DElog.e(TAG, "parseXml() error! xmlParser.parse() failed!");
            return false;
        } else if (xmlParser.check()) {
            DElog.i(TAG, "parseXml() load success!");
            return true;
        } else {
            DElog.e(TAG, "parseXml() error! xmlParser.check() failed!");
            return false;
        }
    }

    private void loadDefaultConfig() {
        DElog.i(TAG, "loadDefaultConfig()");
        mEnable = true;
        mAlgoWideColorSpaceEnable = false;
        mAlgoSkinBeautyEnable = false;
        mAlgoVividEnable = false;
        mNeedNormallizeColorSpace = false;
        mNormallizeColorGamut = ColorspaceType.SRGB;
        mThumbnailStrategy.clear();
        mThumbnailStrategy.put(ThumbnailType.ANIMATION, new ThumbnailStrategy(ImageType.NORMAL, EnumSet.of(AlgoType.ACE)));
        mThumbnailStrategy.put(ThumbnailType.FULLSCREEN, new ThumbnailStrategy(ImageType.NORMAL, EnumSet.of(AlgoType.ACE, AlgoType.SR, AlgoType.SHARPNESS), true));
        mThumbnailStrategy.put(ThumbnailType.HALFSCREEN, new ThumbnailStrategy(ImageType.NORMAL, EnumSet.of(AlgoType.ACE), true));
        mThumbnailStrategy.put(ThumbnailType.DEFAULT, new ThumbnailStrategy(ImageType.NORMAL, EnumSet.of(AlgoType.ACE)));
        mTileStrategy.clear();
        mTileStrategy.put(TileEngineType.NON_SR, new TileStrategy(ImageType.NORMAL, EnumSet.of(AlgoType.ACE)));
        mTileStrategy.put(TileEngineType.SR, new TileStrategy(ImageType.NORMAL, EnumSet.of(AlgoType.SR, AlgoType.SHARPNESS)));
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement rootElement = parser.registerRootElement(new Element_Config());
        rootElement.registerChildElement(new Element_Enable());
        rootElement.registerChildElement(new Element_Custom()).registerChildElement(new Element_CustomGroup());
        HwXmlElement thumbnail = rootElement.registerChildElement(new Element_Thumbnail());
        thumbnail.registerChildElement(new Element_Thumbnail_Algo());
        thumbnail.registerChildElement(new Element_Thumbnail_SaveCommonInfo());
        rootElement.registerChildElement(new Element_Tile()).registerChildElement(new Element_Tile_Algo());
    }
}
