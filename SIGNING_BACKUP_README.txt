MVolaCash Admin - sauvegarde de la signature Android
====================================================

MVolaCashAdmin-release.jks est indispensable pour publier les futures mises à
jour de MVolaCash Admin.

Conserver une sauvegarde sécurisée du fichier .jks ainsi que les identifiants
nécessaires séparément.

La perte de cette clé empêchera Android d'installer une future version comme
mise à jour de cette installation.

Ne jamais ajouter le fichier .jks, ses mots de passe ou une clé privée au dépôt
Git. Ne jamais remplacer ou régénérer automatiquement ce keystore s'il manque.

Configuration locale du build release
--------------------------------------

1. Placer MVolaCashAdmin-release.jks à la racine du projet (ou indiquer son
   chemin absolu dans storeFile).
2. Copier keystore.properties.example vers keystore.properties.
3. Renseigner localement storeFile, storePassword, keyAlias et keyPassword.
4. Exécuter : ./gradlew assembleRelease
5. Récupérer : app/build/outputs/apk/release/app-release.apk

À la place de keystore.properties, un environnement d'intégration continue peut
définir les quatre variables suivantes :

MVOLACASH_RELEASE_STORE_FILE
MVOLACASH_RELEASE_STORE_PASSWORD
MVOLACASH_RELEASE_KEY_ALIAS
MVOLACASH_RELEASE_KEY_PASSWORD

Les deux clés ont des rôles distincts et doivent rester indépendantes :

- MVolaCashAdmin-release.jks signe l'APK Android et permet ses mises à jour ;
- l'alias Android Keystore mvolacash_activation_signing_key est la clé ECDSA
  MVACT1 qui signe les licences MVolaCash Client.
