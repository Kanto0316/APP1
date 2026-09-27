# Protocole d’activation hors ligne

Le code de demande contient exactement 32 caractères de l’alphabet non ambigu
`23456789ABCDEFGHJKMNPQRSTUVWXYZ`. La casse est ignorée. Les tirets et espaces
ASCII sont seulement visuels et sont retirés. L’affichage recommandé est en
groupes de quatre caractères.

Admin signe en UTF-8 la forme canonique
`MVOLACASH|1|PERMANENT|<INSTALLATION_ID>` avec ECDSA P-256 et SHA-256
(`SHA256withECDSA`). Le code retourné est
`MVACT1.<payload UTF-8 en Base64URL sans padding>.<signature DER en Base64URL sans padding>`.
La clé publique affichée est un SubjectPublicKeyInfo X.509 encodé en Base64.

La paire est créée une seule fois sous l’alias Android Keystore
`mvolacash_activation_signing_key`. La clé privée n’est jamais exportée.

> **Sauvegarde :** la suppression des données de MVolaCash Admin ou la perte du
> téléphone Admin peut entraîner la perte de la clé d’activation présente dans
> Android Keystore. Aucune exportation non sécurisée de la clé privée n’est
> fournie. Une stratégie de sauvegarde/clé maître doit être définie avant la
> mise en production.
