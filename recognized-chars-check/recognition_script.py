import cv2
import json
import numpy as np
import easyocr
from pathlib import Path


def main():
    IMAGE_PATH = '1_otsuюpng'
    JSON_PATH = '1_marksjson'
    GROUND_TRUTH_PATH = 'ground_truth.txt'
    SCALE = 4.0
        
    reader = easyocr.Reader(['ru', 'en'], gpu=False, verbose=False)
    
    image = cv2.imread(IMAGE_PATH)
    if image is None:
        print(f'ошибка: файл не удалось загрузить')
        return
    
    with open(JSON_PATH, 'r', encoding='utf-8') as file:
        data = json.load(file)
    marks = data.get('marks', [])
    
    ground_truth = []
    if Path(GROUND_TRUTH_PATH).exists():
        with open(GROUND_TRUTH_PATH, 'r', encoding='utf-8') as file:
            ground_truth = [line.strip() for line in file.readlines()]
        print(f'загружено {len(marks)} символов')
    
    results = []
    correct_count = 0
    
    print(f'распознавание')
    for i, mark in enumerate(marks):
        x1 = int(mark['point1']['x'])
        y1 = int(mark['point1']['y'])
        x2 = int(mark['point2']['x'])
        y2 = int(mark['point2']['y'])
        
        roi = image[y1:y2, x1:x2]
        roi_scaled = cv2.resize(roi, (0, 0), fx=SCALE, fy=SCALE, interpolation=cv2.INTER_CUBIC)
        
        roi_padded = cv2.copyMakeBorder(roi_scaled, 10, 10, 10, 10, cv2.BORDER_CONSTANT, value=255)
                
        ocr_result = reader.readtext(
            roi_padded,
            # detail=0,
            # paragraph=False
            detail=0,
            paragraph=False,
            min_size=10,        # Минимальный размер символа
            text_threshold=0.3, # Порог уверенности (ниже = чувствительнее)
            low_text=0.2,       # Низкий порог текста
            contrast_ths=0.1,   # Контраст
            adjust_contrast=0.5, # Коррекция контраста
            filter_ths=0.001   # Фильтр
        )
        
        recognized = ''.join(ocr_result).strip().upper()
        
        if not recognized:
            recognized = '?'

        true_value = ground_truth[i] if i < len(ground_truth) else None
        is_correct = (recognized == true_value.upper()) if true_value else False
        
        if is_correct:
            correct_count += 1
        
        results.append({
            'index': i,
            'recognized': recognized,
            'true': true_value,
            'correct': is_correct
        })
        
    total = len(results)
    accuracy = (correct_count / total * 100) if total > 0 else 0
    
    print(f'\n\nВсего символов: {total}')
    print(f'Распознано верно:   {correct_count}')
    print(f'Ошибок:             {total - correct_count}')
    print(f'Точность:           {accuracy:.2f}%\n\n')
    
    outpu_file = Path(IMAGE_PATH).name + '.json'
    with open(outpu_file, 'w', encoding='utf-8') as file:
        json.dump({
            'total': total,
            'correct': correct_count,
            'accuracy': accuracy,
            'results': results,
        }, file, ensure_ascii=False, indent=2)
    
    print(f'\nРезультаты сохранены в {outpu_file}')
        



if __name__ == '__main__':
    main()