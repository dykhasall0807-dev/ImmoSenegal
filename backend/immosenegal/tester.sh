#!/bin/bash
# Vérifie les règles de sécurité avec les 4 comptes de démonstration
URL=http://localhost:8080
CORPS='{"titre":"Test securite","description":"Test","type":"VENTE","prix":1,"surface":1,"adresse":"Test","ville":"Ziguinchor"}'

jeton() {
  curl -s -X POST "$URL/api/auth/login" -H "Content-Type: application/json" \
    -d "{\"email\":\"$1\",\"motDePasse\":\"Passer123!\"}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p'
}

verifier() { # description, attendu, obtenu
  if [ "$3" = "$2" ]; then echo "  OK   $1 (code $3)"; else echo "  ECHEC $1 : attendu $2, obtenu $3"; fi
}

echo "Connexion des 4 comptes de démonstration..."
ADMIN=$(jeton admin@immosenegal.sn)
PROPRIO=$(jeton proprio@immosenegal.sn)
CLIENT=$(jeton client@immosenegal.sn)
AGENT=$(jeton agent@immosenegal.sn)
for t in "$ADMIN" "$PROPRIO" "$CLIENT" "$AGENT"; do
  [ -z "$t" ] && echo "  ECHEC : une connexion n'a pas donné de jeton" 
done

echo "Tests des droits :"
verifier "Visiteur consulte le catalogue" 200 "$(curl -s -o /dev/null -w '%{http_code}' $URL/api/biens)"
verifier "Visiteur essaie de créer un bien" 401 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $URL/api/biens -H 'Content-Type: application/json' -d "$CORPS")"
verifier "Client essaie de créer un bien" 403 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $URL/api/biens -H "Authorization: Bearer $CLIENT" -H 'Content-Type: application/json' -d "$CORPS")"
verifier "Propriétaire crée un bien" 200 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $URL/api/biens -H "Authorization: Bearer $PROPRIO" -H 'Content-Type: application/json' -d "$CORPS")"
verifier "Propriétaire essaie la gestion des comptes" 403 "$(curl -s -o /dev/null -w '%{http_code}' $URL/api/admin/utilisateurs -H "Authorization: Bearer $PROPRIO")"
verifier "Agent essaie la gestion des comptes" 403 "$(curl -s -o /dev/null -w '%{http_code}' $URL/api/admin/utilisateurs -H "Authorization: Bearer $AGENT")"
verifier "Admin liste les comptes" 200 "$(curl -s -o /dev/null -w '%{http_code}' $URL/api/admin/utilisateurs -H "Authorization: Bearer $ADMIN")"
verifier "Connexion avec un mauvais mot de passe" 401 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $URL/api/auth/login -H 'Content-Type: application/json' -d '{"email":"client@immosenegal.sn","motDePasse":"mauvais"}')"
verifier "Inscription publique en SUPER_ADMIN refusée" 403 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $URL/api/auth/register -H 'Content-Type: application/json' -d '{"nom":"Pirate","email":"pirate@test.sn","motDePasse":"motdepasse1","role":"SUPER_ADMIN"}')"
