package android.content.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Printer;
import android.util.Slog;
import java.text.Collator;
import java.util.Comparator;

public class ResolveInfo implements Parcelable {
    public static final Creator<ResolveInfo> CREATOR = new Creator<ResolveInfo>() {
        public ResolveInfo createFromParcel(Parcel source) {
            return new ResolveInfo(source, null);
        }

        public ResolveInfo[] newArray(int size) {
            return new ResolveInfo[size];
        }
    };
    private static final String TAG = "ResolveInfo";
    public ActivityInfo activityInfo;
    public AuxiliaryResolveInfo auxiliaryInfo;
    public IntentFilter filter;
    public boolean handleAllWebDataURI;
    public int icon;
    public int iconResourceId;
    @Deprecated
    public boolean instantAppAvailable;
    public boolean isDefault;
    public boolean isInstantAppAvailable;
    public int labelRes;
    public int match;
    public boolean noResourceId;
    public CharSequence nonLocalizedLabel;
    public int preferredOrder;
    public int priority;
    public ProviderInfo providerInfo;
    public String resolvePackageName;
    public ServiceInfo serviceInfo;
    public int specificIndex;
    public boolean system;
    public int targetUserId;

    public static class DisplayNameComparator implements Comparator<ResolveInfo> {
        private final Collator mCollator = Collator.getInstance();
        private PackageManager mPM;

        public DisplayNameComparator(PackageManager pm) {
            this.mPM = pm;
            this.mCollator.setStrength(0);
        }

        public final int compare(ResolveInfo a, ResolveInfo b) {
            if (a.targetUserId != -2) {
                return 1;
            }
            if (b.targetUserId != -2) {
                return -1;
            }
            CharSequence sa = a.loadLabel(this.mPM);
            if (sa == null) {
                sa = a.activityInfo.name;
            }
            CharSequence sb = b.loadLabel(this.mPM);
            if (sb == null) {
                sb = b.activityInfo.name;
            }
            return this.mCollator.compare(sa.toString(), sb.toString());
        }
    }

    /* synthetic */ ResolveInfo(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    public ComponentInfo getComponentInfo() {
        if (this.activityInfo != null) {
            return this.activityInfo;
        }
        if (this.serviceInfo != null) {
            return this.serviceInfo;
        }
        if (this.providerInfo != null) {
            return this.providerInfo;
        }
        throw new IllegalStateException("Missing ComponentInfo!");
    }

    public CharSequence loadLabel(PackageManager pm) {
        if (this.nonLocalizedLabel != null) {
            return this.nonLocalizedLabel;
        }
        CharSequence label;
        if (!(this.resolvePackageName == null || this.labelRes == 0)) {
            CharSequence label2 = pm.getText(this.resolvePackageName, this.labelRes, null);
            if (label2 != null) {
                return label2.toString().trim();
            }
        }
        ComponentInfo ci = getComponentInfo();
        ApplicationInfo ai = ci.applicationInfo;
        if (this.labelRes != 0) {
            label = pm.getText(ci.packageName, this.labelRes, ai);
            if (label != null) {
                return label.toString().trim();
            }
        }
        label = ci.loadLabel(pm);
        if (label != null) {
            label = label.toString().trim();
        }
        return label;
    }

    public int resolveLabelResId() {
        if (this.labelRes != 0) {
            return this.labelRes;
        }
        ComponentInfo componentInfo = getComponentInfo();
        if (componentInfo.labelRes != 0) {
            return componentInfo.labelRes;
        }
        return componentInfo.applicationInfo.labelRes;
    }

    public int resolveIconResId() {
        if (this.icon != 0) {
            return this.icon;
        }
        ComponentInfo componentInfo = getComponentInfo();
        if (componentInfo.icon != 0) {
            return componentInfo.icon;
        }
        return componentInfo.applicationInfo.icon;
    }

    public Drawable loadIcon(PackageManager pm) {
        Drawable dr = null;
        HwThemeManager.updateResolveInfoIconCache(this, this.icon, this.resolvePackageName);
        if (!(this.resolvePackageName == null || this.iconResourceId == 0)) {
            dr = pm.getDrawable(this.resolvePackageName, this.iconResourceId, null);
        }
        ComponentInfo ci = getComponentInfo();
        if (dr == null && this.iconResourceId != 0) {
            dr = pm.getDrawable(ci.packageName, this.iconResourceId, ci.applicationInfo);
        }
        if (dr != null) {
            return pm.getUserBadgedIcon(dr, new UserHandle(pm.getUserId()));
        }
        return ci.loadIcon(pm);
    }

    final int getIconResourceInternal() {
        HwThemeManager.updateResolveInfoIconCache(this, this.icon, null);
        if (this.iconResourceId != 0) {
            return this.iconResourceId;
        }
        ComponentInfo ci = getComponentInfo();
        if (ci != null) {
            return ci.getIconResource();
        }
        return 0;
    }

    public final int getIconResource() {
        if (this.noResourceId) {
            return 0;
        }
        return getIconResourceInternal();
    }

    public void dump(Printer pw, String prefix) {
        dump(pw, prefix, 3);
    }

    public void dump(Printer pw, String prefix, int dumpFlags) {
        StringBuilder stringBuilder;
        StringBuilder stringBuilder2;
        if (this.filter != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(prefix);
            stringBuilder.append("Filter:");
            pw.println(stringBuilder.toString());
            IntentFilter intentFilter = this.filter;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(prefix);
            stringBuilder2.append("  ");
            intentFilter.dump(pw, stringBuilder2.toString());
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(prefix);
        stringBuilder.append("priority=");
        stringBuilder.append(this.priority);
        stringBuilder.append(" preferredOrder=");
        stringBuilder.append(this.preferredOrder);
        stringBuilder.append(" match=0x");
        stringBuilder.append(Integer.toHexString(this.match));
        stringBuilder.append(" specificIndex=");
        stringBuilder.append(this.specificIndex);
        stringBuilder.append(" isDefault=");
        stringBuilder.append(this.isDefault);
        pw.println(stringBuilder.toString());
        if (this.resolvePackageName != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(prefix);
            stringBuilder.append("resolvePackageName=");
            stringBuilder.append(this.resolvePackageName);
            pw.println(stringBuilder.toString());
        }
        if (!(this.labelRes == 0 && this.nonLocalizedLabel == null && this.icon == 0)) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(prefix);
            stringBuilder.append("labelRes=0x");
            stringBuilder.append(Integer.toHexString(this.labelRes));
            stringBuilder.append(" nonLocalizedLabel=");
            stringBuilder.append(this.nonLocalizedLabel);
            stringBuilder.append(" icon=0x");
            stringBuilder.append(Integer.toHexString(this.icon));
            pw.println(stringBuilder.toString());
        }
        if (this.activityInfo != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(prefix);
            stringBuilder.append("ActivityInfo:");
            pw.println(stringBuilder.toString());
            ActivityInfo activityInfo = this.activityInfo;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(prefix);
            stringBuilder2.append("  ");
            activityInfo.dump(pw, stringBuilder2.toString(), dumpFlags);
        } else if (this.serviceInfo != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(prefix);
            stringBuilder.append("ServiceInfo:");
            pw.println(stringBuilder.toString());
            ServiceInfo serviceInfo = this.serviceInfo;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(prefix);
            stringBuilder2.append("  ");
            serviceInfo.dump(pw, stringBuilder2.toString(), dumpFlags);
        } else if (this.providerInfo != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(prefix);
            stringBuilder.append("ProviderInfo:");
            pw.println(stringBuilder.toString());
            ProviderInfo providerInfo = this.providerInfo;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(prefix);
            stringBuilder2.append("  ");
            providerInfo.dump(pw, stringBuilder2.toString(), dumpFlags);
        }
    }

    public ResolveInfo() {
        this.specificIndex = -1;
        this.targetUserId = -2;
    }

    public ResolveInfo(ResolveInfo orig) {
        this.specificIndex = -1;
        this.activityInfo = orig.activityInfo;
        this.serviceInfo = orig.serviceInfo;
        this.providerInfo = orig.providerInfo;
        this.filter = orig.filter;
        this.priority = orig.priority;
        this.preferredOrder = orig.preferredOrder;
        this.match = orig.match;
        this.specificIndex = orig.specificIndex;
        this.labelRes = orig.labelRes;
        this.nonLocalizedLabel = orig.nonLocalizedLabel;
        this.icon = orig.icon;
        this.resolvePackageName = orig.resolvePackageName;
        this.noResourceId = orig.noResourceId;
        this.iconResourceId = orig.iconResourceId;
        this.system = orig.system;
        this.targetUserId = orig.targetUserId;
        this.handleAllWebDataURI = orig.handleAllWebDataURI;
        this.isInstantAppAvailable = orig.isInstantAppAvailable;
        this.instantAppAvailable = this.isInstantAppAvailable;
    }

    public String toString() {
        ComponentInfo ci = getComponentInfo();
        StringBuilder sb = new StringBuilder(128);
        sb.append("ResolveInfo{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        ComponentName.appendShortString(sb, ci.packageName, ci.name);
        if (this.priority != 0) {
            sb.append(" p=");
            sb.append(this.priority);
        }
        if (this.preferredOrder != 0) {
            sb.append(" o=");
            sb.append(this.preferredOrder);
        }
        sb.append(" m=0x");
        sb.append(Integer.toHexString(this.match));
        if (this.targetUserId != -2) {
            sb.append(" targetUserId=");
            sb.append(this.targetUserId);
        }
        sb.append('}');
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        if (this.activityInfo != null) {
            dest.writeInt(1);
            this.activityInfo.writeToParcel(dest, parcelableFlags);
        } else if (this.serviceInfo != null) {
            dest.writeInt(2);
            this.serviceInfo.writeToParcel(dest, parcelableFlags);
        } else if (this.providerInfo != null) {
            dest.writeInt(3);
            this.providerInfo.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(0);
        }
        if (this.filter != null) {
            dest.writeInt(1);
            this.filter.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.priority);
        dest.writeInt(this.preferredOrder);
        dest.writeInt(this.match);
        dest.writeInt(this.specificIndex);
        dest.writeInt(this.labelRes);
        TextUtils.writeToParcel(this.nonLocalizedLabel, dest, parcelableFlags);
        dest.writeInt(this.icon);
        dest.writeString(this.resolvePackageName);
        dest.writeInt(this.targetUserId);
        dest.writeInt(this.system);
        dest.writeInt(this.noResourceId);
        dest.writeInt(this.iconResourceId);
        dest.writeInt(this.handleAllWebDataURI);
        dest.writeInt(this.isInstantAppAvailable);
    }

    private ResolveInfo(Parcel source) {
        this.specificIndex = -1;
        this.activityInfo = null;
        this.serviceInfo = null;
        this.providerInfo = null;
        switch (source.readInt()) {
            case 1:
                this.activityInfo = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(source);
                break;
            case 2:
                this.serviceInfo = (ServiceInfo) ServiceInfo.CREATOR.createFromParcel(source);
                break;
            case 3:
                this.providerInfo = (ProviderInfo) ProviderInfo.CREATOR.createFromParcel(source);
                break;
            default:
                Slog.w(TAG, "Missing ComponentInfo!");
                break;
        }
        if (source.readInt() != 0) {
            this.filter = (IntentFilter) IntentFilter.CREATOR.createFromParcel(source);
        }
        this.priority = source.readInt();
        this.preferredOrder = source.readInt();
        this.match = source.readInt();
        this.specificIndex = source.readInt();
        this.labelRes = source.readInt();
        this.nonLocalizedLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.icon = source.readInt();
        this.resolvePackageName = source.readString();
        this.targetUserId = source.readInt();
        boolean z = false;
        this.system = source.readInt() != 0;
        this.noResourceId = source.readInt() != 0;
        this.iconResourceId = source.readInt();
        this.handleAllWebDataURI = source.readInt() != 0;
        if (source.readInt() != 0) {
            z = true;
        }
        this.isInstantAppAvailable = z;
        this.instantAppAvailable = z;
    }
}
