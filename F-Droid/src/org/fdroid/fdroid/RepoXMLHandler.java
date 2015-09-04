/*
 * Copyright (C) 2010-12  Ciaran Gultnieks, ciaran@ciarang.com
 * Copyright (C) 2009  Roberto Jacinto, roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.fdroid.fdroid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.data.Repo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the index.xml into Java data structures.
 *
 * For streaming apks from an index file, it is helpful if the index has the <repo> tag before
 * any <application> tags. This means that apps and apks can be saved instantly by the RepoUpdater,
 * without having to buffer them at all, saving memory. The XML spec doesn't mandate order like
 * this, though it is almost always a fair assumption:
 *
 *   http://www.ibm.com/developerworks/library/x-eleord/index.html
 *
 * This is doubly so, as repo indices are likely from fdroidserver, which will output everybodys
 * repo the same way. Having said that, this also should not be _forced_ upon people, but we can
 * at least consider rejecting malformed indexes.
 */
public class RepoXMLHandler extends DefaultHandler {

    // The repo we're processing.
    private final Repo repo;

    private List<Apk> apksList = new ArrayList<>();

    private App curapp = null;
    private Apk curapk = null;

    private String currentApkHashType = null;

    // After processing the XML, these will be -1 if the index didn't specify
    // them - otherwise it will be the value specified.
    private int repoMaxAge = -1;
    private int repoVersion = 0;
    private String repoDescription;
    private String repoName;

    // the X.509 signing certificate stored in the header of index.xml
    private String repoSigningCert;

    private final StringBuilder curchars = new StringBuilder();

    interface IndexReceiver {
        void receiveRepo(String name, String description, String signingCert, int maxage, int version);
        void receiveApp(App app, List<Apk> packages);
    }

    private IndexReceiver receiver;

    public RepoXMLHandler(Repo repo, @NonNull IndexReceiver receiver) {
        this.repo = repo;
        this.receiver = receiver;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        curchars.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String curel, String qName)
            throws SAXException {

        final String str = curchars.toString().trim();
        final boolean empty = TextUtils.isEmpty(str);

        if (curel.equals("application") && curapp != null) {
            onApplicationParsed();
        } else if (curel.equals("package") && curapk != null && curapp != null) {
            apksList.add(curapk);
            curapk = null;
        } else if (curel.equals("repo")) {
            onRepoParsed();
        } else if (!empty && curapk != null) {
            switch (curel) {
            case "version":
                curapk.version = str;
                break;
            case "versioncode":
                curapk.vercode = Utils.parseInt(str, -1);
                break;
            case "size":
                curapk.size = Utils.parseInt(str, 0);
                break;
            case "hash":
                if (currentApkHashType == null || currentApkHashType.equals("md5")) {
                    if (curapk.hash == null) {
                        curapk.hash = str;
                        curapk.hashType = "MD5";
                    }
                } else if (currentApkHashType.equals("sha256")) {
                    curapk.hash = str;
                    curapk.hashType = "SHA-256";
                }
                break;
            case "sig":
                curapk.sig = str;
                break;
            case "srcname":
                curapk.srcname = str;
                break;
            case "apkname":
                curapk.apkName = str;
                break;
            case "sdkver":
                curapk.minSdkVersion = Utils.parseInt(str, 0);
                break;
            case "maxsdkver":
                curapk.maxSdkVersion = Utils.parseInt(str, 0);
                break;
            case "added":
                curapk.added = Utils.parseDate(str, null);
                break;
            case "permissions":
                curapk.permissions = Utils.CommaSeparatedList.make(str);
                break;
            case "features":
                curapk.features = Utils.CommaSeparatedList.make(str);
                break;
            case "nativecode":
                curapk.nativecode = Utils.CommaSeparatedList.make(str);
                break;
            }
        } else if (!empty && curapp != null) {
            switch (curel) {
            case "name":
                curapp.name = str;
                break;
            case "icon":
                curapp.icon = str;
                break;
            case "description":
                // This is the old-style description. We'll read it
                // if present, to support old repos, but in newer
                // repos it will get overwritten straight away!
                curapp.description = "<p>" + str + "</p>";
                break;
            case "desc":
                // New-style description.
                curapp.description = str;
                break;
            case "summary":
                curapp.summary = str;
                break;
            case "license":
                curapp.license = str;
                break;
            case "source":
                curapp.sourceURL = str;
                break;
            case "changelog":
                curapp.changelogURL = str;
                break;
            case "donate":
                curapp.donateURL = str;
                break;
            case "bitcoin":
                curapp.bitcoinAddr = str;
                break;
            case "litecoin":
                curapp.litecoinAddr = str;
                break;
            case "dogecoin":
                curapp.dogecoinAddr = str;
                break;
            case "flattr":
                curapp.flattrID = str;
                break;
            case "web":
                curapp.webURL = str;
                break;
            case "tracker":
                curapp.trackerURL = str;
                break;
            case "added":
                curapp.added = Utils.parseDate(str, null);
                break;
            case "lastupdated":
                curapp.lastUpdated = Utils.parseDate(str, null);
                break;
            case "marketversion":
                curapp.upstreamVersion = str;
                break;
            case "marketvercode":
                curapp.upstreamVercode = Utils.parseInt(str, -1);
                break;
            case "categories":
                curapp.categories = Utils.CommaSeparatedList.make(str);
                break;
            case "antifeatures":
                curapp.antiFeatures = Utils.CommaSeparatedList.make(str);
                break;
            case "requirements":
                curapp.requirements = Utils.CommaSeparatedList.make(str);
                break;
            }
        } else if (!empty && curel.equals("description")) {
            repoDescription = cleanWhiteSpace(str);
        }
    }

    private void onApplicationParsed() {
        receiver.receiveApp(curapp, apksList);
        curapp = null;
        apksList = new ArrayList<>();
        // If the app id is already present in this apps list, then it
        // means the same index file has a duplicate app, which should
        // not be allowed.
        // However, I'm thinking that it should be undefined behaviour,
        // because it is probably a bug in the fdroid server that made it
        // happen, and I don't *think* it will crash the client, because
        // the first app will insert, the second one will update the newly
        // inserted one.
    }

    private void onRepoParsed() {
        receiver.receiveRepo(repoName, repoDescription, repoSigningCert, repoMaxAge, repoVersion);
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (localName.equals("repo")) {
            repoSigningCert = attributes.getValue("", "pubkey");
            repoMaxAge = Utils.parseInt(attributes.getValue("", "maxage"), -1);
            repoVersion = Utils.parseInt(attributes.getValue("", "version"), -1);
            repoName = cleanWhiteSpace(attributes.getValue("", "name"));
            repoDescription = cleanWhiteSpace(attributes.getValue("", "description"));
        } else if (localName.equals("application") && curapp == null) {
            curapp = new App();
            curapp.id = attributes.getValue("", "id");
        } else if (localName.equals("package") && curapp != null && curapk == null) {
            curapk = new Apk();
            curapk.id = curapp.id;
            curapk.repo = repo.getId();
            currentApkHashType = null;

        } else if (localName.equals("hash") && curapk != null) {
            currentApkHashType = attributes.getValue("", "type");
        }
        curchars.setLength(0);
    }

    private String cleanWhiteSpace(@Nullable String str) {
        return str == null ? null : str.replaceAll("\\s", " ");
    }
}
