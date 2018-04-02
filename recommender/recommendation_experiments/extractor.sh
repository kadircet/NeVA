mysql -u neva -D neva -e "select * from user_choice_history inner join suggestee on suggestee_id=suggestee.id ;" -B | tr '\t' ',' > dataset.csv
